package ru.fkdev.donmarket.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ru.fkdev.donmarket.DonMarketPlugin;
import ru.fkdev.donmarket.util.ItemBuilder;

public class CreateOrderMainGui extends GuiBase {

    private static final String P = "create-order-menu";

    private int backSlot, privSlot, casesSlot, tokensSlot, otherSlot;

    public CreateOrderMainGui(DonMarketPlugin plugin, Player player) {
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

        backSlot = cfg.getSlot(P + ".back-button", 4);
        inventory.setItem(backSlot, new ItemBuilder(cfg.getMaterial(P + ".back-button", Material.TIPPED_ARROW))
                .name(cfg.getName(P + ".back-button", "Вернуться.."))
                .lore(cfg.getLore(P + ".back-button"))
                .hideAll().build());

        privSlot = cfg.getSlot(P + ".categories.privileges", 13);
        inventory.setItem(privSlot, new ItemBuilder(cfg.getMaterial(P + ".categories.privileges", Material.TOTEM_OF_UNDYING))
                .name(cfg.getName(P + ".categories.privileges", "Привилегии"))
                .lore(cfg.getLore(P + ".categories.privileges"))
                .hideAll().build());

        casesSlot = cfg.getSlot(P + ".categories.cases", 21);
        inventory.setItem(casesSlot, new ItemBuilder(cfg.getMaterial(P + ".categories.cases", Material.ENDER_CHEST))
                .name(cfg.getName(P + ".categories.cases", "Кейсы"))
                .lore(cfg.getLore(P + ".categories.cases"))
                .hideAll().build());

        tokensSlot = cfg.getSlot(P + ".categories.tokens", 23);
        inventory.setItem(tokensSlot, new ItemBuilder(cfg.getMaterial(P + ".categories.tokens", Material.EMERALD))
                .name(cfg.getName(P + ".categories.tokens", "Токены"))
                .lore(cfg.getLore(P + ".categories.tokens"))
                .hideAll().build());

        otherSlot = cfg.getSlot(P + ".categories.other", 31);
        inventory.setItem(otherSlot, new ItemBuilder(cfg.getMaterial(P + ".categories.other", Material.NETHER_STAR))
                .name(cfg.getName(P + ".categories.other", "Разное"))
                .lore(cfg.getLore(P + ".categories.other"))
                .hideAll().build());

        fillEmpty();
    }

    @Override
    public void onClick(int slot, ClickType clickType) {
        if (slot == backSlot) new DonMarketMainGui(plugin, player).open();
        else if (slot == privSlot) new CreateOrderPrivilegesGui(plugin, player).open();
        else if (slot == casesSlot) new CreateOrderCasesGui(plugin, player).open();
        else if (slot == tokensSlot) new CreateOrderTokensGui(plugin, player).open();
        else if (slot == otherSlot) new CreateOrderOtherGui(plugin, player).open();
    }
}
