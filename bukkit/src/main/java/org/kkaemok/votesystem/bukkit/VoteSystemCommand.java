package org.kkaemok.votesystem.bukkit;

import org.kkaemok.votesystem.core.VoteConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class VoteSystemCommand implements CommandExecutor {
    private final VoteSystemBukkitPlugin plugin;

    public VoteSystemCommand(VoteSystemBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ChatColor.RED + "Usage: /votesystem reload");
            return true;
        }

        VoteConfig config = plugin.getVoteConfig();
        if (sender instanceof Player player) {
            if (!player.hasPermission(config.commandPermission())) {
                player.sendMessage(ChatColor.RED + "You do not have permission.");
                return true;
            }
        }

        plugin.reloadAll();
        sender.sendMessage(ChatColor.GREEN + "VoteSystem reloaded.");
        return true;
    }
}
