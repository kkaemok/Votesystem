package org.kkaemok.votesystem.bukkit;

import org.kkaemok.votesystem.core.VoteConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

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
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /votesystem <reload|remind [player|all]>");
            return true;
        }

        VoteConfig config = plugin.getVoteConfig();
        if (sender instanceof Player player) {
            if (!player.hasPermission(config.commandPermission())) {
                player.sendMessage(ChatColor.RED + "You do not have permission.");
                return true;
            }
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);
        switch (subCommand) {
            case "reload" -> {
                plugin.reloadAll();
                sender.sendMessage(ChatColor.GREEN + "VoteSystem reloaded.");
            }
            case "remind" -> {
                if (args.length >= 2 && !args[1].equalsIgnoreCase("all")) {
                    Player target = Bukkit.getPlayerExact(args[1]);
                    if (target == null) {
                        sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                        return true;
                    }
                    plugin.sendRecommendReminder(target);
                    sender.sendMessage(ChatColor.GREEN + "Reminder sent to " + target.getName() + ".");
                    return true;
                }
                plugin.broadcastRecommendReminder();
                sender.sendMessage(ChatColor.GREEN + "Reminder broadcast sent.");
            }
            default -> sender.sendMessage(ChatColor.RED + "Usage: /votesystem <reload|remind [player|all]>");
        }
        return true;
    }
}
