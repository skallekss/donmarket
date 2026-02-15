package ru.fkdev.donmarket.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ru.fkdev.donmarket.DonMarketPlugin;
import ru.fkdev.donmarket.model.MarketOrder;
import ru.fkdev.donmarket.model.OrderStatus;
import ru.fkdev.donmarket.util.ItemBuilder;

import java.util.List;
import java.util.Map;

public class MyOffersGui extends GuiBase {

    private static final String P = "my-offers-menu";
    private static final int ITEMS_PER_PAGE = 45;

    private int backSlot;

    public MyOffersGui(DonMarketPlugin plugin, Player player) {
        super(plugin, player);
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
        List<MarketOrder> myOrders = plugin.getStorage().getOrdersByPlayer(player.getUniqueId());

        if (myOrders.isEmpty()) {
            for (int i = 0; i < getSize(); i++) {
                inventory.setItem(i, orangeGlass());
            }
            int emptySlot = cfg.getSlot(P + ".empty-item", 22);
            inventory.setItem(emptySlot, new ItemBuilder(cfg.getMaterial(P + ".empty-item", Material.DEAD_BUSH))
                    .name(cfg.getName(P + ".empty-item", "Пустая страница"))
                    .lore(cfg.getLore(P + ".empty-item"))
                    .hideAll().build());
        } else {
            for (int i = 0; i < ITEMS_PER_PAGE; i++) {
                if (i < myOrders.size()) {
                    inventory.setItem(i, buildMyOrderCard(myOrders.get(i)));
                } else {
                    inventory.setItem(i, orangeGlass());
                }
            }
        }

        for (int i = ITEMS_PER_PAGE; i < getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, redGlass());
            }
        }

        backSlot = cfg.getSlot(P + ".back-button", 49);
        inventory.setItem(backSlot, new ItemBuilder(cfg.getMaterial(P + ".back-button", Material.TIPPED_ARROW))
                .name(cfg.getName(P + ".back-button", "Вернуться.."))
                .lore(cfg.getLore(P + ".back-button"))
                .hideAll().build());
    }

    private ItemStack buildMyOrderCard(MarketOrder order) {
        String catKey = order.getCategory() != null ? order.getCategory().name() : "DEFAULT";
        String matStr = cfg.getString("orders-menu.order-card.materials." + catKey, null);
        if (matStr == null) matStr = cfg.getString("orders-menu.order-card.materials.DEFAULT", "GOLD_INGOT");
        Material mat;
        try { mat = Material.valueOf(matStr); } catch (IllegalArgumentException e) { mat = Material.GOLD_INGOT; }

        Map<String, String> ph = Map.of(
                "{product}", order.getProductKey(),
                "{status}", order.getStatus().name(),
                "{amount}", String.format("%.0f", order.getCoinsOffer()),
                "{token_price}", String.valueOf(order.getTokenPrice()),
                "{expires}", order.getRemainingFormatted()
        );

        String name = cfg.getStringColored(P + ".order-card.name", "&#FFFF00❖ &#FFFFFF{product}");
        for (var e : ph.entrySet()) name = name.replace(e.getKey(), e.getValue());

        List<String> lore = cfg.getLoreWithPlaceholders(P + ".order-card.lore", ph);

        return new ItemBuilder(mat).name(name).lore(lore).hideAll().build();
    }

    @Override
    public void onClick(int slot, ClickType clickType) {
        if (slot == backSlot) {
            new PlayerOrdersGui(plugin, player).open();
            return;
        }

        if (slot >= 0 && slot < ITEMS_PER_PAGE) {
            List<MarketOrder> myOrders = plugin.getStorage().getOrdersByPlayer(player.getUniqueId());
            if (slot < myOrders.size()) {
                MarketOrder order = myOrders.get(slot);
                if (clickType == ClickType.RIGHT) {
                    plugin.getStorage().updateOrderStatus(order.getId(), OrderStatus.CANCELLED);
                    player.sendMessage(cfg.msg("order-cancelled", Map.of("{order_id}", String.valueOf(order.getId()))));
                    player.playSound(player.getLocation(), cfg.getSound("cancel"), cfg.getSoundVolume("cancel"), cfg.getSoundPitch("cancel"));
                    refresh();
                } else if (clickType == ClickType.LEFT) {
                    Map<String, String> ph = Map.of(
                            "{order_id}", String.valueOf(order.getId()),
                            "{product}", order.getProductKey(),
                            "{amount}", String.format("%.0f", order.getCoinsOffer()),
                            "{token_price}", String.valueOf(order.getTokenPrice()),
                            "{expires}", order.getRemainingFormatted()
                    );
                    player.sendMessage(cfg.msg("order-info-header", ph));
                    player.sendMessage(cfg.msg("order-info-product", ph));
                    player.sendMessage(cfg.msg("order-info-amount", ph));
                    player.sendMessage(cfg.msg("order-info-price", ph));
                    player.sendMessage(cfg.msg("order-info-expires", ph));
                }
            }
        }
    }
}
