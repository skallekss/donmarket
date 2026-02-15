package ru.fkdev.donmarket.economy;

import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.fkdev.donmarket.storage.Storage;

import java.util.UUID;
import java.util.logging.Logger;

public class EconomyManager {

    private final Storage storage;
    private final Logger logger;
    private Economy vaultEconomy;
    private boolean vaultEnabled;
    private PlayerPointsAPI playerPointsAPI;
    private boolean playerPointsEnabled;

    public EconomyManager(Storage storage, Logger logger) {
        this.storage = storage;
        this.logger = logger;
    }

    public void init() {
        // Vault
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                vaultEconomy = rsp.getProvider();
                vaultEnabled = true;
                logger.info("[DonMarket] Vault economy hooked successfully.");
            }
        }
        if (!vaultEnabled) {
            logger.info("[DonMarket] Vault not found, using internal coin balance.");
        }

        // PlayerPoints
        Plugin ppPlugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");
        if (ppPlugin instanceof PlayerPoints pp) {
            playerPointsAPI = pp.getAPI();
            playerPointsEnabled = true;
            logger.info("[DonMarket] PlayerPoints hooked successfully.");
        } else {
            playerPointsEnabled = false;
            logger.info("[DonMarket] PlayerPoints not found, using internal token balance.");
        }
    }

    // ========== Coins (Vault / fallback) ==========

    public double getCoins(Player player) {
        if (vaultEnabled) {
            return vaultEconomy.getBalance(player);
        }
        return storage.getCoinBalance(player.getUniqueId());
    }

    public boolean withdrawCoins(Player player, double amount) {
        if (vaultEnabled) {
            if (vaultEconomy.getBalance(player) < amount) return false;
            vaultEconomy.withdrawPlayer(player, amount);
            return true;
        }
        double bal = storage.getCoinBalance(player.getUniqueId());
        if (bal < amount) return false;
        storage.addCoinBalance(player.getUniqueId(), -amount);
        return true;
    }

    public void depositCoins(Player player, double amount) {
        if (vaultEnabled) {
            vaultEconomy.depositPlayer(player, amount);
        } else {
            storage.addCoinBalance(player.getUniqueId(), amount);
        }
    }

    public void depositCoins(UUID uuid, String name, double amount) {
        if (vaultEnabled) {
            vaultEconomy.depositPlayer(Bukkit.getOfflinePlayer(uuid), amount);
        } else {
            storage.addCoinBalance(uuid, amount);
        }
    }

    // ========== Tokens / Points (PlayerPoints / fallback) ==========

    public int getTokens(Player player) {
        if (playerPointsEnabled) {
            return playerPointsAPI.look(player.getUniqueId());
        }
        return (int) storage.getTokenBalance(player.getUniqueId());
    }

    public boolean withdrawTokens(Player player, int amount) {
        if (playerPointsEnabled) {
            if (playerPointsAPI.look(player.getUniqueId()) < amount) return false;
            return playerPointsAPI.take(player.getUniqueId(), amount);
        }
        double bal = storage.getTokenBalance(player.getUniqueId());
        if (bal < amount) return false;
        storage.addTokenBalance(player.getUniqueId(), -amount);
        return true;
    }

    public void depositTokens(Player player, int amount) {
        if (playerPointsEnabled) {
            playerPointsAPI.give(player.getUniqueId(), amount);
        } else {
            storage.addTokenBalance(player.getUniqueId(), amount);
        }
    }

    public void depositTokens(UUID uuid, int amount) {
        if (playerPointsEnabled) {
            playerPointsAPI.give(uuid, amount);
        } else {
            storage.addTokenBalance(uuid, amount);
        }
    }

    public boolean isVaultEnabled() {
        return vaultEnabled;
    }

    public boolean isPlayerPointsEnabled() {
        return playerPointsEnabled;
    }
}
