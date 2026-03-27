package org.kkaemok.votesystem.bukkit;

import org.kkaemok.votesystem.core.VoteConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class RecommendCommand implements CommandExecutor {
    private final VoteSystemBukkitPlugin plugin;

    public RecommendCommand(VoteSystemBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return true;
        }

        VoteConfig config = plugin.getVoteConfig();
        String link = config.recommendLink();
        if (link == null || link.isBlank()) {
            player.sendMessage(ChatColor.RED + "추천 링크가 설정되지 않았습니다.");
            return true;
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', link));
        return true;
    }
}
