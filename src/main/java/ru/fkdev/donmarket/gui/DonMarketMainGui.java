package ru.fkdev.donmarket.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ru.fkdev.donmarket.DonMarketPlugin;
import ru.fkdev.donmarket.util.ItemBuilder;

public class DonMarketMainGui extends GuiBase {

    private static final String P = "main-menu";

    private int ordersSlot;
    private int createSlot;

    public DonMarketMainGui(DonMarketPlugin plugin, Player player) {
        super(plugin, player);
    }

    @Override
    protected String getTitle() {
        return cfg.getTitle(P);
    }

    @Override
    protected int getSize() {
        return cfg.getSize(P, 45);
    }

    @Override
    protected void render() {
        fillBackground(cfg.getIntList(P + ".red-slots"), cfg.getIntList(P + ".orange-slots"));

        ordersSlot = cfg.getSlot(P + ".orders-button", 21);
        inventory.setItem(ordersSlot, new ItemBuilder(cfg.getMaterial(P + ".orders-button", org.bukkit.Material.GOLD_INGOT))
                .name(cfg.getName(P + ".orders-button", "Заказы игроков"))
                .lore(cfg.getLore(P + ".orders-button"))
                .hideAll()
                .build());

        createSlot = cfg.getSlot(P + ".create-button", 23);
        inventory.setItem(createSlot, new ItemBuilder(cfg.getMaterial(P + ".create-button", org.bukkit.Material.TOTEM_OF_UNDYING))
                .name(cfg.getName(P + ".create-button", "Создать заказ"))
                .lore(cfg.getLore(P + ".create-button"))
                .hideAll()
                .build());

        fillEmpty();
    }

    @Override
    public void onClick(int slot, ClickType clickType) {
        if (slot == ordersSlot) {
            new PlayerOrdersGui(plugin, player).open();
        } else if (slot == createSlot) {
            new CreateOrderMainGui(plugin, player).open();
        }
    }
}
