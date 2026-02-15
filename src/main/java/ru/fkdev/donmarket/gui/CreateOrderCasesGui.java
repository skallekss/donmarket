package ru.fkdev.donmarket.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import ru.fkdev.donmarket.DonMarketPlugin;
import ru.fkdev.donmarket.model.Category;
import ru.fkdev.donmarket.model.PlayerUIState;
import ru.fkdev.donmarket.util.ColorUtil;
import ru.fkdev.donmarket.util.ItemBuilder;
import ru.fkdev.donmarket.util.SkullUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateOrderCasesGui extends GuiBase {

    private static final String P = "cases-menu";

    private int backSlot;

    private record CaseData(int slot, String name, double minPrice, boolean available, String material, String texture) {}

    private final List<CaseData> cases = new ArrayList<>();

    public CreateOrderCasesGui(DonMarketPlugin plugin, Player player) {
        super(plugin, player);
        loadCases();
    }

    private void loadCases() {
        cases.clear();
        List<Map<?, ?>> items = cfg.getMapList(P + ".items");
        for (Map<?, ?> map : items) {
            int slot = ((Number) map.get("slot")).intValue();
            String name = (String) map.get("name");
            double minPrice = ((Number) map.get("min-price")).doubleValue();
            boolean available = map.containsKey("available") ? (Boolean) map.get("available") : true;
            Object matObj = map.get("material");
            String material = matObj != null ? (String) matObj : "PLAYER_HEAD";
            Object texObj = map.get("texture");
            String texture = texObj != null ? (String) texObj : "";
            cases.add(new CaseData(slot, name, minPrice, available, material, texture));
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

        for (CaseData c : cases) {
            inventory.setItem(c.slot(), buildCaseItem(c));
        }
        fillEmpty();
    }

    private ItemStack buildCaseItem(CaseData c) {
        Map<String, String> ph = Map.of("{min_price}", String.format("%.0f", c.minPrice()));
        List<String> lore = c.available()
                ? cfg.getLoreWithPlaceholders(P + ".available-lore", ph)
                : cfg.getLoreWithPlaceholders(P + ".unavailable-lore", ph);

        if ("PLAYER_HEAD".equals(c.material()) && !c.texture().isEmpty()) {
            ItemStack skull = SkullUtil.createSkull(c.texture());
            var meta = skull.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ColorUtil.colorize("&#FFFF00[&#FF6600⚡&#FFFF00] &#FFFFFF" + c.name()));
                meta.setLore(lore);
                meta.addItemFlags(ItemFlag.values());
                skull.setItemMeta(meta);
            }
            return skull;
        }

        Material mat;
        try { mat = Material.valueOf(c.material()); } catch (Exception e) { mat = Material.CHEST; }
        return new ItemBuilder(mat)
                .name(ColorUtil.colorize("&#FFFF00[&#FF6600⚡&#FFFF00] &#FFFFFF" + c.name()))
                .lore(lore).hideAll().build();
    }

    @Override
    public void onClick(int slot, ClickType clickType) {
        if (slot == backSlot) {
            new CreateOrderMainGui(plugin, player).open();
            return;
        }
        for (CaseData c : cases) {
            if (c.slot() == slot) {
                if (!c.available()) {
                    player.playSound(player.getLocation(), cfg.getSound("unavailable"),
                            cfg.getSoundVolume("unavailable"), cfg.getSoundPitch("unavailable"));
                    return;
                }
                if (clickType == ClickType.LEFT) {
                    player.closeInventory();
                    Map<String, String> ph = Map.of("{product}", c.name(), "{min_price}", String.format("%.0f", c.minPrice()));
                    player.sendMessage(cfg.msg("chat-prompt-line1", ph));
                    player.sendMessage(cfg.msg("chat-prompt-line2", ph));
                    player.sendMessage(cfg.msg("chat-prompt-line3", ph));

                    PlayerUIState state = plugin.getUIState(player);
                    state.setPendingChatInput(true);
                    state.setPendingProductType("CASE");
                    state.setPendingProductKey(c.name());
                    state.setPendingDuration(null);
                    state.setPendingMinPrice(c.minPrice());
                    state.setPendingCategory(Category.CASES);
                    state.setChatInputTimeout(System.currentTimeMillis() + cfg.getChatInputTimeoutSeconds() * 1000L);
                    plugin.scheduleChatTimeout(player);
                }
                return;
            }
        }
    }
}
