package ru.fkdev.donmarket.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import ru.fkdev.donmarket.DonMarketPlugin;
import ru.fkdev.donmarket.model.Category;
import ru.fkdev.donmarket.model.PlayerUIState;
import ru.fkdev.donmarket.util.ColorUtil;
import ru.fkdev.donmarket.util.ItemBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateOrderTokensGui extends GuiBase {

    private static final String P = "tokens-menu";

    private int backSlot;

    private record TokenData(int slot, String name, int tokenAmount, double minPrice, Material material, List<String> lore) {}

    private final List<TokenData> tokens = new ArrayList<>();

    public CreateOrderTokensGui(DonMarketPlugin plugin, Player player) {
        super(plugin, player);
        loadTokens();
    }

    private void loadTokens() {
        tokens.clear();
        List<Map<?, ?>> items = cfg.getMapList(P + ".items");
        for (Map<?, ?> map : items) {
            int slot = ((Number) map.get("slot")).intValue();
            String name = (String) map.get("name");
            int tokenAmount = ((Number) map.get("token-amount")).intValue();
            double minPrice = ((Number) map.get("min-price")).doubleValue();
            Material mat;
            try { mat = Material.valueOf((String) map.get("material")); } catch (Exception e) { mat = Material.EMERALD; }
            List<String> lore = new ArrayList<>();
            Object loreObj = map.get("lore");
            if (loreObj instanceof List<?> list) {
                for (Object o : list) lore.add(ColorUtil.colorize(o.toString()));
            }
            tokens.add(new TokenData(slot, name, tokenAmount, minPrice, mat, lore));
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

        for (TokenData t : tokens) {
            inventory.setItem(t.slot(), new ItemBuilder(t.material())
                    .name(ColorUtil.colorize("&#FFFF00[&#FF6600⚡&#FFFF00] &#FFFFFF" + t.name()))
                    .lore(t.lore())
                    .hideAll().build());
        }
        fillEmpty();
    }

    @Override
    public void onClick(int slot, ClickType clickType) {
        if (slot == backSlot) {
            new CreateOrderMainGui(plugin, player).open();
            return;
        }
        for (TokenData t : tokens) {
            if (t.slot() == slot && clickType == ClickType.LEFT) {
                player.closeInventory();
                String productLabel = t.name() + " (" + t.tokenAmount() + "⚡)";
                Map<String, String> ph = Map.of("{product}", productLabel, "{min_price}", String.format("%.0f", t.minPrice()));
                player.sendMessage(cfg.msg("chat-prompt-line1", ph));
                player.sendMessage(cfg.msg("chat-prompt-line2", ph));
                player.sendMessage(cfg.msg("chat-prompt-line3", ph));

                PlayerUIState state = plugin.getUIState(player);
                state.setPendingChatInput(true);
                state.setPendingProductType("TOKEN");
                state.setPendingProductKey(t.name());
                state.setPendingDuration(String.valueOf(t.tokenAmount()));
                state.setPendingMinPrice(t.minPrice());
                state.setPendingCategory(Category.TOKENS);
                state.setChatInputTimeout(System.currentTimeMillis() + cfg.getChatInputTimeoutSeconds() * 1000L);
                plugin.scheduleChatTimeout(player);
                return;
            }
        }
    }
}
