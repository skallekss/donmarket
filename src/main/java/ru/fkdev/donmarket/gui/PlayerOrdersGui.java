package ru.fkdev.donmarket.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ru.fkdev.donmarket.DonMarketPlugin;
import ru.fkdev.donmarket.model.*;
import ru.fkdev.donmarket.util.ColorUtil;
import ru.fkdev.donmarket.util.ItemBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerOrdersGui extends GuiBase {

    private static final String P = "orders-menu";

    private int itemsPerPage;
    private int myOffersSlot, prevSlot, refreshSlot, nextSlot, catSlot;

    private int page = 0;
    private SortMode sortMode = SortMode.BEST_RATIO;
    private Category categoryFilter = Category.ALL;

    public PlayerOrdersGui(DonMarketPlugin plugin, Player player) {
        super(plugin, player);
        PlayerUIState state = plugin.getUIState(player);
        this.page = state.getPage();
        this.sortMode = state.getSortMode();
        this.categoryFilter = state.getCategoryFilter();
    }

    @Override
    protected String getTitle() {
        return cfg.getTitle(P);
    }

    @Override
    protected int getSize() {
        return cfg.getSize(P, 54);
    }

    @Override
    protected void render() {
        itemsPerPage = cfg.getInt(P + ".items-per-page", 45);

        List<MarketOrder> orders = plugin.getStorage().getActiveOrders(
                categoryFilter, sortMode, page * itemsPerPage, itemsPerPage);

        for (int i = 0; i < itemsPerPage; i++) {
            if (i < orders.size()) {
                inventory.setItem(i, buildOrderCard(orders.get(i)));
            } else {
                inventory.setItem(i, orangeGlass());
            }
        }

        for (int i = itemsPerPage; i < getSize(); i++) {
            inventory.setItem(i, redGlass());
        }

        // My Offers button
        myOffersSlot = cfg.getSlot(P + ".my-offers-button", 46);
        inventory.setItem(myOffersSlot, new ItemBuilder(cfg.getMaterial(P + ".my-offers-button", Material.ENDER_CHEST))
                .name(cfg.getName(P + ".my-offers-button", "Мои предложения"))
                .lore(cfg.getLore(P + ".my-offers-button"))
                .hideAll().build());

        // Prev page
        prevSlot = cfg.getSlot(P + ".prev-page-button", 48);
        inventory.setItem(prevSlot, new ItemBuilder(cfg.getMaterial(P + ".prev-page-button", Material.TIPPED_ARROW))
                .name(cfg.getName(P + ".prev-page-button", "Предыдущая страница"))
                .lore(cfg.getLoreWithPlaceholders(P + ".prev-page-button.lore",
                        Map.of("{page}", String.valueOf(page + 1))))
                .hideAll().build());

        // Refresh / Sort
        refreshSlot = cfg.getSlot(P + ".refresh-button", 49);
        inventory.setItem(refreshSlot, buildSortItem());

        // Next page
        nextSlot = cfg.getSlot(P + ".next-page-button", 50);
        inventory.setItem(nextSlot, new ItemBuilder(cfg.getMaterial(P + ".next-page-button", Material.SPECTRAL_ARROW))
                .name(cfg.getName(P + ".next-page-button", "Следующая страница"))
                .lore(cfg.getLoreWithPlaceholders(P + ".next-page-button.lore",
                        Map.of("{page}", String.valueOf(page + 1))))
                .hideAll().build());

        // Category
        catSlot = cfg.getSlot(P + ".category-button", 52);
        inventory.setItem(catSlot, buildCategoryItem());
    }

    private ItemStack buildSortItem() {
        List<String> lore = new ArrayList<>(cfg.getStringListColored(P + ".refresh-button.lore-header"));
        String activeTpl = cfg.getString(P + ".refresh-button.sort-active", " &#FF0000▶ {mode}");
        String inactiveTpl = cfg.getString(P + ".refresh-button.sort-inactive", " &#777777  {mode}");
        for (SortMode mode : SortMode.values()) {
            String tpl = (mode == sortMode) ? activeTpl : inactiveTpl;
            lore.add(ColorUtil.colorize(tpl.replace("{mode}", mode.getDisplayName())));
        }
        lore.addAll(cfg.getStringListColored(P + ".refresh-button.lore-footer"));

        return new ItemBuilder(cfg.getMaterial(P + ".refresh-button", Material.NETHER_STAR))
                .name(cfg.getName(P + ".refresh-button", "Обновить"))
                .lore(lore).hideAll().build();
    }

    private ItemStack buildCategoryItem() {
        List<String> lore = new ArrayList<>(cfg.getStringListColored(P + ".category-button.lore-header"));
        String activeTpl = cfg.getString(P + ".category-button.cat-active", " &#FF0000▶ {category}");
        String inactiveTpl = cfg.getString(P + ".category-button.cat-inactive", " &#777777  {category}");
        for (Category cat : Category.values()) {
            String tpl = (cat == categoryFilter) ? activeTpl : inactiveTpl;
            lore.add(ColorUtil.colorize(tpl.replace("{category}", cat.getDisplayName())));
        }
        lore.addAll(cfg.getStringListColored(P + ".category-button.lore-footer"));

        return new ItemBuilder(cfg.getMaterial(P + ".category-button", Material.CHEST))
                .name(cfg.getName(P + ".category-button", "Категория"))
                .lore(lore).hideAll().build();
    }

    private ItemStack buildOrderCard(MarketOrder order) {
        Material mat = getOrderMaterial(order);
        int bal = plugin.getEconomy().getTokens(player);
        Map<String, String> ph = Map.of(
                "{product}", order.getProductKey(),
                "{dealer}", order.getOwnerName(),
                "{amount}", String.format("%.0f", order.getCoinsOffer()),
                "{rate}", String.format("%.2f", order.getRate()),
                "{expires}", order.getRemainingFormatted(),
                "{token_price}", String.valueOf(order.getTokenPrice()),
                "{balance}", String.valueOf(bal)
        );
        String name = cfg.getStringColored(P + ".order-card.name", "&#FFFF00❖ &#FFFFFF{product}");
        for (var e : ph.entrySet()) name = name.replace(e.getKey(), e.getValue());

        List<String> lore = cfg.getLoreWithPlaceholders(P + ".order-card.lore", ph);

        return new ItemBuilder(mat).name(name).lore(lore).hideAll().build();
    }

    private Material getOrderMaterial(MarketOrder order) {
        String catKey = order.getCategory() != null ? order.getCategory().name() : "DEFAULT";
        String matStr = cfg.getString(P + ".order-card.materials." + catKey, null);
        if (matStr == null) matStr = cfg.getString(P + ".order-card.materials.DEFAULT", "GOLD_INGOT");
        try {
            return Material.valueOf(matStr);
        } catch (IllegalArgumentException e) {
            return Material.GOLD_INGOT;
        }
    }

    @Override
    public void onClick(int slot, ClickType clickType) {
        if (slot >= 0 && slot < itemsPerPage) {
            List<MarketOrder> orders = plugin.getStorage().getActiveOrders(
                    categoryFilter, sortMode, page * itemsPerPage, itemsPerPage);
            if (slot < orders.size()) {
                handleBuyOrder(orders.get(slot));
            }
            return;
        }

        if (slot == myOffersSlot) {
            new MyOffersGui(plugin, player).open();
        } else if (slot == prevSlot) {
            if (page > 0) { page--; saveState(); refresh(); }
        } else if (slot == refreshSlot) {
            if (clickType == ClickType.RIGHT) {
                sortMode = sortMode.next(); page = 0; saveState(); refresh();
            } else {
                refresh();
            }
        } else if (slot == nextSlot) {
            int total = plugin.getStorage().countActiveOrders(categoryFilter);
            if ((page + 1) * itemsPerPage < total) { page++; saveState(); refresh(); }
        } else if (slot == catSlot) {
            if (clickType == ClickType.LEFT) {
                categoryFilter = categoryFilter.next(); page = 0; saveState(); refresh();
            }
        }
    }

    private void handleBuyOrder(MarketOrder order) {
        if (order.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(cfg.msg("buy-own-order"));
            player.playSound(player.getLocation(), cfg.getSound("error"), cfg.getSoundVolume("error"), cfg.getSoundPitch("error"));
            return;
        }

        int tokenBal = plugin.getEconomy().getTokens(player);
        if (tokenBal < order.getTokenPrice()) {
            player.sendMessage(cfg.msg("not-enough-points", Map.of(
                    "{token_price}", String.valueOf(order.getTokenPrice()),
                    "{balance}", String.valueOf(tokenBal))));
            player.playSound(player.getLocation(), cfg.getSound("error"), cfg.getSoundVolume("error"), cfg.getSoundPitch("error"));
            return;
        }

        plugin.getEconomy().withdrawTokens(player, order.getTokenPrice());
        plugin.getEconomy().depositCoins(player, order.getCoinsOffer());
        plugin.getStorage().updateOrderStatus(order.getId(), OrderStatus.FILLED);

        // Execute reward commands from config (type-specific + default)
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (String cmdTpl : cfg.getRewardCommands(order.getProductType())) {
                String cmd = cmdTpl
                        .replace("{player}", player.getName())
                        .replace("{owner}", order.getOwnerName())
                        .replace("{product}", order.getProductKey())
                        .replace("{duration}", order.getDurationType() != null ? order.getDurationType() : "")
                        .replace("{duration_code}", order.getExtraJson() != null ? order.getExtraJson() : "")
                        .replace("{order_id}", String.valueOf(order.getId()))
                        .replace("{amount}", String.format("%.0f", order.getCoinsOffer()))
                        .replace("{token_price}", String.valueOf(order.getTokenPrice()));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        });

        player.sendMessage(cfg.msg("buy-success", Map.of("{amount}", String.format("%.0f", order.getCoinsOffer()))));
        player.playSound(player.getLocation(), cfg.getSound("success"), cfg.getSoundVolume("success"), cfg.getSoundPitch("success"));
        refresh();
    }

    private void saveState() {
        PlayerUIState state = plugin.getUIState(player);
        state.setPage(page);
        state.setSortMode(sortMode);
        state.setCategoryFilter(categoryFilter);
    }
}
