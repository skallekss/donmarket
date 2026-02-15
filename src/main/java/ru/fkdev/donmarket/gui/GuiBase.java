package ru.fkdev.donmarket.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import ru.fkdev.donmarket.DonMarketPlugin;
import ru.fkdev.donmarket.config.ConfigManager;
import ru.fkdev.donmarket.util.ItemBuilder;

import java.util.List;

public abstract class GuiBase implements InventoryHolder {

    protected final DonMarketPlugin plugin;
    protected final Player player;
    protected final ConfigManager cfg;
    protected Inventory inventory;

    public GuiBase(DonMarketPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.cfg = plugin.getConfigManager();
    }

    protected abstract String getTitle();

    protected abstract int getSize();

    protected abstract void render();

    public abstract void onClick(int slot, ClickType clickType);

    public void open() {
        inventory = Bukkit.createInventory(this, getSize(), getTitle());
        render();
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void refresh() {
        if (inventory != null) {
            inventory.clear();
            render();
        }
    }

    protected ItemStack redGlass() {
        return new ItemBuilder(cfg.getGlassMaterial("red"))
                .name(cfg.getGlassName("red"))
                .build();
    }

    protected ItemStack orangeGlass() {
        return new ItemBuilder(cfg.getGlassMaterial("orange"))
                .name(cfg.getGlassName("orange"))
                .build();
    }

    protected void fillBackground(List<Integer> redSlots, List<Integer> orangeSlots) {
        ItemStack red = redGlass();
        ItemStack orange = orangeGlass();
        for (int s : redSlots) {
            if (s < getSize()) inventory.setItem(s, red);
        }
        for (int s : orangeSlots) {
            if (s < getSize()) inventory.setItem(s, orange);
        }
    }

    protected void fillBackground(int[] redSlots, int[] orangeSlots) {
        ItemStack red = redGlass();
        ItemStack orange = orangeGlass();
        for (int s : redSlots) {
            if (s < getSize()) inventory.setItem(s, red);
        }
        for (int s : orangeSlots) {
            if (s < getSize()) inventory.setItem(s, orange);
        }
    }

    protected void fillEmpty() {
        ItemStack orange = orangeGlass();
        for (int i = 0; i < getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, orange);
            }
        }
    }

    protected void fillEmptyWith(ItemStack filler) {
        for (int i = 0; i < getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    public Player getPlayer() {
        return player;
    }
}
