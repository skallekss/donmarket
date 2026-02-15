package ru.fkdev.donmarket;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ru.fkdev.donmarket.command.DmCommand;
import ru.fkdev.donmarket.config.ConfigManager;
import ru.fkdev.donmarket.economy.EconomyManager;
import ru.fkdev.donmarket.listener.ChatInputListener;
import ru.fkdev.donmarket.listener.GuiListener;
import ru.fkdev.donmarket.model.PlayerUIState;
import ru.fkdev.donmarket.storage.SQLiteStorage;
import ru.fkdev.donmarket.storage.Storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DonMarketPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private Storage storage;
    private EconomyManager economyManager;
    private final Map<UUID, PlayerUIState> uiStates = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> chatTimeouts = new ConcurrentHashMap<>();

    @Override
    public void onEnable() {
        // Load config
        configManager = new ConfigManager(this);
        configManager.load();

        // Initialize storage
        storage = new SQLiteStorage(getDataFolder(), getLogger());
        storage.init();

        // Initialize economy (Vault + PlayerPoints)
        economyManager = new EconomyManager(storage, getLogger());
        economyManager.init();

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new GuiListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChatInputListener(this), this);

        // Register command
        var cmd = getCommand("dm");
        if (cmd != null) {
            cmd.setExecutor(new DmCommand(this));
        }

        getLogger().info("DonMarket enabled successfully!");
    }

    @Override
    public void onDisable() {
        chatTimeouts.values().forEach(BukkitTask::cancel);
        chatTimeouts.clear();

        if (storage != null) {
            storage.shutdown();
        }

        getLogger().info("DonMarket disabled.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Storage getStorage() {
        return storage;
    }

    public EconomyManager getEconomy() {
        return economyManager;
    }

    public PlayerUIState getUIState(Player player) {
        return uiStates.computeIfAbsent(player.getUniqueId(), k -> new PlayerUIState());
    }

    public void removeUIState(Player player) {
        uiStates.remove(player.getUniqueId());
    }

    public void scheduleChatTimeout(Player player) {
        BukkitTask existing = chatTimeouts.remove(player.getUniqueId());
        if (existing != null) existing.cancel();

        int timeoutSec = configManager.getChatInputTimeoutSeconds();
        long ticks = timeoutSec * 20L;

        BukkitTask task = Bukkit.getScheduler().runTaskLater(this, () -> {
            PlayerUIState state = getUIState(player);
            if (state.isPendingChatInput()) {
                state.clearPending();
                if (player.isOnline()) {
                    player.sendMessage(configManager.msg("chat-timeout"));
                }
            }
            chatTimeouts.remove(player.getUniqueId());
        }, ticks);

        chatTimeouts.put(player.getUniqueId(), task);
    }
}
