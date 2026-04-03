package org.kkaemok.votesystem.bukkit;

import org.kkaemok.votesystem.core.PlayerNameUtil;
import org.kkaemok.votesystem.core.VoteConfig;
import org.kkaemok.votesystem.core.VoteData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerJoinListener implements Listener {
    private final VoteSystemBukkitPlugin plugin;

    public PlayerJoinListener(VoteSystemBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerKey = PlayerNameUtil.normalizeKey(player.getName());
        VoteData data = plugin.getVoteData();
        int pending = data.getPending(playerKey);
        VoteConfig config = plugin.getVoteConfig();
        if (pending > 0) {
            for (int i = 0; i < pending; i++) {
                plugin.dispatchRewardCommands(player.getName());
                data.incrementTotalVotes(playerKey);
            }
            data.clearPending(playerKey);

            if (config.messageEnabled()) {
                String message = ChatColor.translateAlternateColorCodes('&', config.messageVoted());
                player.sendMessage(message);
            }

            plugin.saveData();
        }

        plugin.handleJoinReminder(player);
    }
}
