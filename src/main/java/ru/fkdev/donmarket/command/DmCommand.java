package ru.fkdev.donmarket.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.fkdev.donmarket.DonMarketPlugin;
import ru.fkdev.donmarket.gui.DonMarketMainGui;

public class DmCommand implements CommandExecutor {

    private final DonMarketPlugin plugin;

    public DmCommand(DonMarketPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("donmarket.use")) {
            player.sendMessage(plugin.getConfigManager().msg("no-permission"));
            return true;
        }

        new DonMarketMainGui(plugin, player).open();
        return true;
    }
}
