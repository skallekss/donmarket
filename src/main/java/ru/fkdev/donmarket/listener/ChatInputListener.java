package ru.fkdev.donmarket.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.fkdev.donmarket.DonMarketPlugin;
import ru.fkdev.donmarket.config.ConfigManager;
import ru.fkdev.donmarket.model.Category;
import ru.fkdev.donmarket.model.MarketOrder;
import ru.fkdev.donmarket.model.OrderStatus;
import ru.fkdev.donmarket.model.PlayerUIState;

import java.util.Map;

public class ChatInputListener implements Listener {

    private final DonMarketPlugin plugin;

    public ChatInputListener(DonMarketPlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PlayerUIState state = plugin.getUIState(player);
        ConfigManager cfg = plugin.getConfigManager();

        if (!state.isPendingChatInput()) return;

        event.setCancelled(true);

        String message = event.getMessage().trim();

        // Check timeout
        if (System.currentTimeMillis() > state.getChatInputTimeout()) {
            state.clearPending();
            player.sendMessage(cfg.msg("chat-timeout"));
            return;
        }

        // Cancel keyword
        for (String word : cfg.getCancelWords()) {
            if (message.equalsIgnoreCase(word)) {
                state.clearPending();
                player.sendMessage(cfg.msg("chat-cancelled"));
                return;
            }
        }

        // Parse amount
        String cleaned = message.replaceAll("[\\s,₽$]", "");
        double amount;
        try {
            amount = Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            player.sendMessage(cfg.msg("chat-invalid-number"));
            return;
        }

        if (amount < state.getPendingMinPrice()) {
            player.sendMessage(cfg.msg("chat-below-min",
                    Map.of("{min_price}", String.format("%.0f", state.getPendingMinPrice()))));
            return;
        }

        // Create order
        double coinsPerPoint = cfg.getCoinsPerPoint();
        int tokenPrice = Math.max(1, (int) Math.ceil(amount / coinsPerPoint));
        long expireMs = cfg.getOrderExpireHours() * 3600_000L;

        MarketOrder order = new MarketOrder();
        order.setOwner(player.getUniqueId());
        order.setOwnerName(player.getName());
        order.setCategory(state.getPendingCategory() != null ? state.getPendingCategory() : Category.ALL);
        order.setProductType(state.getPendingProductType());
        order.setProductKey(state.getPendingProductKey());
        order.setDurationType(state.getPendingDuration());
        order.setExtraJson(state.getPendingDurationCode());
        order.setCoinsMin(state.getPendingMinPrice());
        order.setCoinsOffer(amount);
        order.setTokenPrice(tokenPrice);
        order.setCreatedAt(System.currentTimeMillis());
        order.setExpiresAt(System.currentTimeMillis() + expireMs);
        order.setStatus(OrderStatus.ACTIVE);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getStorage().createOrder(order);
            Bukkit.getScheduler().runTask(plugin, () -> {
                String product = order.getProductKey()
                        + (order.getDurationType() != null ? " [" + order.getDurationType() + "]" : "");
                Map<String, String> ph = Map.of(
                        "{order_id}", String.valueOf(order.getId()),
                        "{product}", product,
                        "{amount}", String.format("%.0f", amount),
                        "{token_price}", String.valueOf(order.getTokenPrice())
                );
                player.sendMessage(cfg.msg("order-created-line1", ph));
                player.sendMessage(cfg.msg("order-created-line2", ph));
                player.sendMessage(cfg.msg("order-created-line3", ph));
            });
        });

        state.clearPending();
    }
}
