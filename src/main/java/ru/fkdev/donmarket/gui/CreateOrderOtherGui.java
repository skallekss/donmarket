package ru.fkdev.donmarket.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ru.fkdev.donmarket.DonMarketPlugin;
import ru.fkdev.donmarket.model.Category;
import ru.fkdev.donmarket.model.PlayerUIState;
import ru.fkdev.donmarket.util.ColorUtil;
import ru.fkdev.donmarket.util.ItemBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateOrderOtherGui extends GuiBase {

    private static final String P = "other-menu";

    private int backSlot;

    private record OtherData(int slot, String name, double minPrice, boolean available, Material material, Category category) {}

    private final List<OtherData> otherItems = new ArrayList<>();

    public CreateOrderOtherGui(DonMarketPlugin plugin, Player player) {
        super(plugin, player);
        loadItems();
    }

    private void loadItems() {
        otherItems.clear();
        List<Map<?, ?>> items = cfg.getMapList(P + ".items");
        for (Map<?, ?> map : items) {
            int slot = ((Number) map.get("slot")).intValue();
            String name = (String) map.get("name");
            double minPrice = ((Number) map.get("min-price")).doubleValue();
            boolean available = map.containsKey("available") ? (Boolean) map.get("available") : true;
            Material mat;
            try { mat = Material.valueOf((String) map.get("material")); } catch (Exception e) { mat = Material.NETHER_STAR; }
            Category cat;
            Object catObj = map.get("category");
            try { cat = Category.valueOf(catObj != null ? (String) catObj : "ALL"); } catch (Exception e) { cat = Category.ALL; }
            otherItems.add(new OtherData(slot, name, minPrice, available, mat, cat));
        }
    }

    @Override
    protected String getTitle() { return cfg.getTitle(P); }

    @Override
    protected int getSize() { return cfg.getSize(P, 45); }

    @Override
    protected void render() {
        fillBackground(cfg.getIntList(P + ".red-slots"), cfg.getIntList(P + ".orange-slots"));

        backSlot = cfg.getSlot(P + ".back-button", 4);
        inventory.setItem(backSlot, new ItemBuilder(cfg.getMaterial(P + ".back-button", Material.TIPPED_ARROW))
                .name(cfg.getName(P + ".back-button", "Вернуться.."))
                .lore(cfg.getLore(P + ".back-button"))
                .hideAll().build());

        for (OtherData item : otherItems) {
            inventory.setItem(item.slot(), buildOtherItem(item));
        }
        fillEmpty();
    }

    private ItemStack buildOtherItem(OtherData item) {
        Map<String, String> ph = Map.of("{min_price}", String.format("%.0f", item.minPrice()));
        List<String> lore = item.available()
                ? cfg.getLoreWithPlaceholders(P + ".available-lore", ph)
                : cfg.getLoreWithPlaceholders(P + ".unavailable-lore", ph);

        return new ItemBuilder(item.material())
                .name(ColorUtil.colorize("&#FFFF00[&#FF6600⚡&#FFFF00] &#FFFFFF" + item.name()))
                .lore(lore).hideAll().build();
    }

    @Override
    public void onClick(int slot, ClickType clickType) {
        if (slot == backSlot) {
            new CreateOrderMainGui(plugin, player).open();
            return;
        }
        for (OtherData item : otherItems) {
            if (item.slot() == slot) {
                if (!item.available()) {
                    player.playSound(player.getLocation(), cfg.getSound("unavailable"),
                            cfg.getSoundVolume("unavailable"), cfg.getSoundPitch("unavailable"));
                    return;
                }
                if (clickType == ClickType.LEFT) {
                    player.closeInventory();
                    Map<String, String> ph = Map.of("{product}", item.name(), "{min_price}", String.format("%.0f", item.minPrice()));
                    player.sendMessage(cfg.msg("chat-prompt-line1", ph));
                    player.sendMessage(cfg.msg("chat-prompt-line2", ph));
                    player.sendMessage(cfg.msg("chat-prompt-line3", ph));

                    PlayerUIState state = plugin.getUIState(player);
                    state.setPendingChatInput(true);
                    state.setPendingProductType("OTHER");
                    state.setPendingProductKey(item.name());
                    state.setPendingDuration(null);
                    state.setPendingMinPrice(item.minPrice());
                    state.setPendingCategory(item.category());
                    state.setChatInputTimeout(System.currentTimeMillis() + cfg.getChatInputTimeoutSeconds() * 1000L);
                    plugin.scheduleChatTimeout(player);
                }
                return;
            }
        }
    }
}
