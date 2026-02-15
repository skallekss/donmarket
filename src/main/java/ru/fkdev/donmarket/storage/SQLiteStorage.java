package ru.fkdev.donmarket.storage;

import ru.fkdev.donmarket.model.Category;
import ru.fkdev.donmarket.model.MarketOrder;
import ru.fkdev.donmarket.model.OrderStatus;
import ru.fkdev.donmarket.model.SortMode;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLiteStorage implements Storage {

    private final File dbFile;
    private final Logger logger;
    private Connection connection;

    public SQLiteStorage(File dataFolder, Logger logger) {
        this.dbFile = new File(dataFolder, "donmarket.db");
        this.logger = logger;
    }

    @Override
    public void init() {
        try {
            if (!dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            createTables();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize SQLite", e);
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS market_orders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    owner TEXT NOT NULL,
                    owner_name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    product_type TEXT NOT NULL,
                    product_key TEXT NOT NULL,
                    duration_type TEXT,
                    coins_min REAL NOT NULL DEFAULT 0,
                    coins_offer REAL NOT NULL DEFAULT 0,
                    token_price INTEGER NOT NULL DEFAULT 0,
                    created_at BIGINT NOT NULL,
                    expires_at BIGINT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'ACTIVE',
                    extra_json TEXT
                )
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_balances (
                    uuid TEXT PRIMARY KEY,
                    tokens REAL NOT NULL DEFAULT 0,
                    coins REAL NOT NULL DEFAULT 0
                )
            """);
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_orders_status ON market_orders(status)");
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_orders_owner ON market_orders(owner)");
        }
    }

    @Override
    public void shutdown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to close SQLite connection", e);
        }
    }

    @Override
    public void createOrder(MarketOrder order) {
        String sql = """
            INSERT INTO market_orders (owner, owner_name, category, product_type, product_key,
            duration_type, coins_min, coins_offer, token_price, created_at, expires_at, status, extra_json)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, order.getOwner().toString());
            ps.setString(2, order.getOwnerName());
            ps.setString(3, order.getCategory().name());
            ps.setString(4, order.getProductType());
            ps.setString(5, order.getProductKey());
            ps.setString(6, order.getDurationType());
            ps.setDouble(7, order.getCoinsMin());
            ps.setDouble(8, order.getCoinsOffer());
            ps.setInt(9, order.getTokenPrice());
            ps.setLong(10, order.getCreatedAt());
            ps.setLong(11, order.getExpiresAt());
            ps.setString(12, order.getStatus().name());
            ps.setString(13, order.getExtraJson());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    order.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to create order", e);
        }
    }

    @Override
    public void updateOrderStatus(int orderId, OrderStatus status) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE market_orders SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to update order status", e);
        }
    }

    @Override
    public List<MarketOrder> getActiveOrders(Category category, SortMode sortMode, int offset, int limit) {
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM market_orders WHERE status = 'ACTIVE' AND expires_at > ?");
        if (category != null && category != Category.ALL) {
            sql.append(" AND category = ?");
        }
        String orderBy = switch (sortMode) {
            case BEST_RATIO -> " ORDER BY (coins_offer * 1.0 / CASE WHEN token_price > 0 THEN token_price ELSE 1 END) DESC";
            case MOST_EXPENSIVE_COINS -> " ORDER BY coins_offer DESC";
            case CHEAPEST_COINS -> " ORDER BY coins_offer ASC";
            case MOST_EXPENSIVE_TOKENS -> " ORDER BY token_price DESC";
            case CHEAPEST_TOKENS -> " ORDER BY token_price ASC";
        };
        sql.append(orderBy);
        sql.append(" LIMIT ? OFFSET ?");

        List<MarketOrder> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setLong(idx++, System.currentTimeMillis());
            if (category != null && category != Category.ALL) {
                ps.setString(idx++, category.name());
            }
            ps.setInt(idx++, limit);
            ps.setInt(idx, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapOrder(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get active orders", e);
        }
        return result;
    }

    @Override
    public int countActiveOrders(Category category) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM market_orders WHERE status = 'ACTIVE' AND expires_at > ?");
        if (category != null && category != Category.ALL) {
            sql.append(" AND category = ?");
        }
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setLong(idx++, System.currentTimeMillis());
            if (category != null && category != Category.ALL) {
                ps.setString(idx, category.name());
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to count orders", e);
        }
        return 0;
    }

    @Override
    public List<MarketOrder> getOrdersByPlayer(UUID owner) {
        List<MarketOrder> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM market_orders WHERE owner = ? AND status = 'ACTIVE' ORDER BY created_at DESC")) {
            ps.setString(1, owner.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapOrder(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get player orders", e);
        }
        return result;
    }

    @Override
    public MarketOrder getOrderById(int id) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM market_orders WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapOrder(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get order by id", e);
        }
        return null;
    }

    private MarketOrder mapOrder(ResultSet rs) throws SQLException {
        MarketOrder o = new MarketOrder();
        o.setId(rs.getInt("id"));
        o.setOwner(UUID.fromString(rs.getString("owner")));
        o.setOwnerName(rs.getString("owner_name"));
        try {
            o.setCategory(Category.valueOf(rs.getString("category")));
        } catch (IllegalArgumentException e) {
            o.setCategory(Category.ALL);
        }
        o.setProductType(rs.getString("product_type"));
        o.setProductKey(rs.getString("product_key"));
        o.setDurationType(rs.getString("duration_type"));
        o.setCoinsMin(rs.getDouble("coins_min"));
        o.setCoinsOffer(rs.getDouble("coins_offer"));
        o.setTokenPrice(rs.getInt("token_price"));
        o.setCreatedAt(rs.getLong("created_at"));
        o.setExpiresAt(rs.getLong("expires_at"));
        try {
            o.setStatus(OrderStatus.valueOf(rs.getString("status")));
        } catch (IllegalArgumentException e) {
            o.setStatus(OrderStatus.ACTIVE);
        }
        o.setExtraJson(rs.getString("extra_json"));
        return o;
    }

    private void ensurePlayerRow(UUID player) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR IGNORE INTO player_balances (uuid, tokens, coins) VALUES (?, 0, 0)")) {
            ps.setString(1, player.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to ensure player row", e);
        }
    }

    @Override
    public double getTokenBalance(UUID player) {
        ensurePlayerRow(player);
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT tokens FROM player_balances WHERE uuid = ?")) {
            ps.setString(1, player.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("tokens");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get token balance", e);
        }
        return 0;
    }

    @Override
    public void setTokenBalance(UUID player, double balance) {
        ensurePlayerRow(player);
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE player_balances SET tokens = ? WHERE uuid = ?")) {
            ps.setDouble(1, balance);
            ps.setString(2, player.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to set token balance", e);
        }
    }

    @Override
    public void addTokenBalance(UUID player, double amount) {
        ensurePlayerRow(player);
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE player_balances SET tokens = tokens + ? WHERE uuid = ?")) {
            ps.setDouble(1, amount);
            ps.setString(2, player.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to add token balance", e);
        }
    }

    @Override
    public double getCoinBalance(UUID player) {
        ensurePlayerRow(player);
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT coins FROM player_balances WHERE uuid = ?")) {
            ps.setString(1, player.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("coins");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get coin balance", e);
        }
        return 0;
    }

    @Override
    public void setCoinBalance(UUID player, double balance) {
        ensurePlayerRow(player);
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE player_balances SET coins = ? WHERE uuid = ?")) {
            ps.setDouble(1, balance);
            ps.setString(2, player.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to set coin balance", e);
        }
    }

    @Override
    public void addCoinBalance(UUID player, double amount) {
        ensurePlayerRow(player);
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE player_balances SET coins = coins + ? WHERE uuid = ?")) {
            ps.setDouble(1, amount);
            ps.setString(2, player.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to add coin balance", e);
        }
    }
}
