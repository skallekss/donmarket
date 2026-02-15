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

import java.util.*;

public class CreateOrderPrivilegesGui extends GuiBase {

    private static final String P = "privileges-menu";

    private int backSlot;
    private final Map<Integer, int[]> durationIndexMap = new HashMap<>();

    private record PrivData(int slot, String name, Material material, int r, int g, int b, List<Double> minPrices) {}

    private final List<PrivData> privileges = new ArrayList<>();

    public CreateOrderPrivilegesGui(DonMarketPlugin plugin, Player player) {
        super(plugin, player);
        loadPrivileges();
    }

    private void loadPrivileges() {
        privileges.clear();
        List<Map<?, ?>> items = cfg.getMapList(P + ".items");
        for (Map<?, ?> map : items) {
            int slot = ((Number) map.get("slot")).intValue();
            String name = (String) map.get("name");
            Material mat;
            try { mat = Material.valueOf((String) map.get("material")); } catch (Exception e) { mat = Material.LEATHER_CHESTPLATE; }
            List<?> colorList = (List<?>) map.get("color");
            int r = colorList != null && colorList.size() >= 3 ? ((Number) colorList.get(0)).intValue() : 200;
            int g = colorList != null && colorList.size() >= 3 ? ((Number) colorList.get(1)).intValue() : 200;
            int b = colorList != null && colorList.size() >= 3 ? ((Number) colorList.get(2)).intValue() : 200;
            List<?> priceList = (List<?>) map.get("min-prices");
            List<Double> minPrices = new ArrayList<>();
            if (priceList != null) {
                for (Object o : priceList) minPrices.add(((Number) o).doubleValue());
            }
            while (minPrices.size() < 3) minPrices.add(100.0);
            privileges.add(new PrivData(slot, name, mat, r, g, b, minPrices));
            durationIndexMap.putIfAbsent(slot, new int[]{0});
        }
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

        List<String> durationLabels = cfg.getCfg().getStringList(P + ".duration-labels");
        if (durationLabels.isEmpty()) durationLabels = List.of("30 дней", "90 дней", "навсегда");

        String activeTpl = cfg.getString(P + ".duration-active", " &#FF0000▶ {label} (Мин. сумма: ${price})");
        String inactiveTpl = cfg.getString(P + ".duration-inactive", " &#777777  {label} (Мин. сумма: ${price})");
        List<String> footer = cfg.getStringListColored(P + ".privilege-lore-footer");

        for (PrivData priv : privileges) {
            int durIdx = durationIndexMap.getOrDefault(priv.slot(), new int[]{0})[0];
            double currentMin = priv.minPrices().get(durIdx);

            List<String> lore = new ArrayList<>();
            lore.add("");
            for (int i = 0; i < durationLabels.size(); i++) {
                String tpl = (i == durIdx) ? activeTpl : inactiveTpl;
                lore.add(ColorUtil.colorize(tpl
                        .replace("{label}", durationLabels.get(i))
                        .replace("{price}", String.format("%.0f", priv.minPrices().get(i)))));
            }
            for (String line : footer) {
                lore.add(line.replace("{min_price}", String.format("%.0f", currentMin)));
            }

            inventory.setItem(priv.slot(), new ItemBuilder(priv.material())
                    .name(ColorUtil.colorize("&#FFFF00[&#FF6600⚡&#FFFF00] &#FFFFFF" + priv.name()))
                    .lore(lore).hideAll()
                    .leatherColor(priv.r(), priv.g(), priv.b())
                    .build());
        }

        fillEmpty();
    }

    @Override
    public void onClick(int slot, ClickType clickType) {
        if (slot == backSlot) {
            new CreateOrderMainGui(plugin, player).open();
            return;
        }

        List<String> durationLabels = cfg.getCfg().getStringList(P + ".duration-labels");
        if (durationLabels.isEmpty()) durationLabels = List.of("30 дней", "90 дней", "навсегда");

        for (PrivData priv : privileges) {
            if (priv.slot() == slot) {
                int[] durArr = durationIndexMap.get(priv.slot());
                if (durArr == null) durArr = new int[]{0};

                if (clickType == ClickType.RIGHT) {
                    durArr[0] = (durArr[0] + 1) % durationLabels.size();
                    durationIndexMap.put(priv.slot(), durArr);
                    refresh();
                } else if (clickType == ClickType.LEFT) {
                    int durIdx = durArr[0];
                    double minPrice = priv.minPrices().get(durIdx);
                    String duration = durationLabels.get(durIdx);

                    List<String> durationCodes = cfg.getCfg().getStringList(P + ".duration-codes");
                    String durationCode = (durationCodes.size() > durIdx) ? durationCodes.get(durIdx) : duration;

                    String productLabel = "Привилегия: " + priv.name() + " [" + duration + "]";

                    player.closeInventory();

                    Map<String, String> ph = Map.of(
                            "{product}", productLabel,
                            "{min_price}", String.format("%.0f", minPrice)
                    );
                    player.sendMessage(cfg.msg("chat-prompt-line1", ph));
                    player.sendMessage(cfg.msg("chat-prompt-line2", ph));
                    player.sendMessage(cfg.msg("chat-prompt-line3", ph));

                    PlayerUIState state = plugin.getUIState(player);
                    state.setPendingChatInput(true);
                    state.setPendingProductType("PRIVILEGE");
                    state.setPendingProductKey(priv.name());
                    state.setPendingDuration(duration);
                    state.setPendingDurationCode(durationCode);
                    state.setPendingMinPrice(minPrice);
                    state.setPendingCategory(Category.PRIVILEGES);
                    state.setChatInputTimeout(System.currentTimeMillis() + cfg.getChatInputTimeoutSeconds() * 1000L);

                    plugin.scheduleChatTimeout(player);
                }
                return;
            }
        }
    }
}
