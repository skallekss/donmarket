package ru.fkdev.donmarket.config;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.fkdev.donmarket.DonMarketPlugin;
import ru.fkdev.donmarket.util.ColorUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigManager {

    private final DonMarketPlugin plugin;
    private FileConfiguration cfg;

    public ConfigManager(DonMarketPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.cfg = plugin.getConfig();
    }

    public FileConfiguration getCfg() {
        return cfg;
    }

    // ========== Settings ==========

    public int getOrderExpireHours() {
        return cfg.getInt("settings.order-expire-hours", 24);
    }

    public int getChatInputTimeoutSeconds() {
        return cfg.getInt("settings.chat-input-timeout-seconds", 60);
    }

    public double getCoinsPerPoint() {
        return cfg.getDouble("settings.coins-per-point", 10);
    }

    public List<String> getCancelWords() {
        String raw = cfg.getString("settings.cancel-words", "cancel,отмена");
        List<String> words = new ArrayList<>();
        for (String w : raw.split(",")) {
            words.add(w.trim().toLowerCase());
        }
        return words;
    }

    // ========== Sounds ==========

    public Sound getSound(String key) {
        try {
            return Sound.valueOf(cfg.getString("sounds." + key, "ENTITY_VILLAGER_NO"));
        } catch (IllegalArgumentException e) {
            return Sound.ENTITY_VILLAGER_NO;
        }
    }

    public float getSoundVolume(String key) {
        return (float) cfg.getDouble("sounds." + key + "-volume", 1.0);
    }

    public float getSoundPitch(String key) {
        return (float) cfg.getDouble("sounds." + key + "-pitch", 1.0);
    }

    // ========== Messages ==========

    public String msg(String key) {
        return ColorUtil.colorize(cfg.getString("messages." + key, "§cMissing message: " + key));
    }

    public String msg(String key, Map<String, String> placeholders) {
        String raw = cfg.getString("messages." + key, "§cMissing message: " + key);
        for (var entry : placeholders.entrySet()) {
            raw = raw.replace(entry.getKey(), entry.getValue());
        }
        return ColorUtil.colorize(raw);
    }

    // ========== Rewards ==========

    public List<String> getRewardCommands(String productType) {
        List<String> commands = new ArrayList<>();
        // Type-specific commands (PRIVILEGE, CASE, TOKEN, OTHER)
        if (productType != null && cfg.contains("rewards." + productType)) {
            commands.addAll(cfg.getStringList("rewards." + productType));
        }
        // Default commands (always executed)
        if (cfg.contains("rewards.default")) {
            commands.addAll(cfg.getStringList("rewards.default"));
        }
        return commands;
    }

    // ========== GUI Background ==========

    public Material getGlassMaterial(String type) {
        try {
            return Material.valueOf(cfg.getString("gui-background." + type + ".material", "RED_STAINED_GLASS_PANE"));
        } catch (IllegalArgumentException e) {
            return type.equals("red") ? Material.RED_STAINED_GLASS_PANE : Material.ORANGE_STAINED_GLASS_PANE;
        }
    }

    public String getGlassName(String type) {
        return ColorUtil.colorize(cfg.getString("gui-background." + type + ".name", "FunTime.su"));
    }

    // ========== Generic GUI helpers ==========

    public String getTitle(String menuPath) {
        return ColorUtil.colorize(cfg.getString(menuPath + ".title", "Menu"));
    }

    public int getSize(String menuPath, int def) {
        return cfg.getInt(menuPath + ".size", def);
    }

    public List<Integer> getIntList(String path) {
        return cfg.getIntegerList(path);
    }

    public int getSlot(String path, int def) {
        return cfg.getInt(path + ".slot", def);
    }

    public Material getMaterial(String path, Material def) {
        try {
            return Material.valueOf(cfg.getString(path + ".material", def.name()));
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    public String getName(String path, String def) {
        return ColorUtil.colorize(cfg.getString(path + ".name", def));
    }

    public List<String> getLore(String path) {
        if (!cfg.contains(path + ".lore")) return Collections.emptyList();
        return cfg.getStringList(path + ".lore").stream()
                .map(ColorUtil::colorize)
                .collect(Collectors.toList());
    }

    public List<String> getStringListColored(String path) {
        if (!cfg.contains(path)) return Collections.emptyList();
        return cfg.getStringList(path).stream()
                .map(ColorUtil::colorize)
                .collect(Collectors.toList());
    }

    public String getString(String path, String def) {
        return cfg.getString(path, def);
    }

    public String getStringColored(String path, String def) {
        return ColorUtil.colorize(cfg.getString(path, def));
    }

    public double getDouble(String path, double def) {
        return cfg.getDouble(path, def);
    }

    public int getInt(String path, int def) {
        return cfg.getInt(path, def);
    }

    public boolean getBoolean(String path, boolean def) {
        return cfg.getBoolean(path, def);
    }

    public ConfigurationSection getSection(String path) {
        return cfg.getConfigurationSection(path);
    }

    public List<Map<?, ?>> getMapList(String path) {
        return cfg.getMapList(path);
    }

    // ========== Lore with placeholders ==========

    public List<String> getLoreWithPlaceholders(String path, Map<String, String> placeholders) {
        if (!cfg.contains(path)) return Collections.emptyList();
        List<String> raw = cfg.getStringList(path);
        List<String> result = new ArrayList<>();
        for (String line : raw) {
            for (var entry : placeholders.entrySet()) {
                line = line.replace(entry.getKey(), entry.getValue());
            }
            result.add(ColorUtil.colorize(line));
        }
        return result;
    }
}
