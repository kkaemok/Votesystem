package org.kkaemok.votesystem.bukkit;

import org.kkaemok.votesystem.core.PlayerNameUtil;
import org.kkaemok.votesystem.core.VoteConfig;
import org.kkaemok.votesystem.core.VoteData;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class VoteListener implements Listener {
    private final VoteSystemBukkitPlugin plugin;

    public VoteListener(VoteSystemBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        Vote vote = event.getVote();
        if (vote == null || vote.getUsername() == null) {
            return;
        }
        String playerName = vote.getUsername().trim();
        if (playerName.isEmpty()) {
            return;
        }

        VoteConfig config = plugin.getVoteConfig();
        VoteData data = plugin.getVoteData();
        String playerKey = PlayerNameUtil.normalizeKey(playerName);

        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            data.addPending(playerKey, 1);
            plugin.saveData();
            return;
        }

        plugin.dispatchRewardCommands(player.getName());
        data.incrementTotalVotes(playerKey);

        if (config.messageEnabled()) {
            String message = ChatColor.translateAlternateColorCodes('&', config.messageVoted());
            player.sendMessage(message);
        }

        plugin.saveData();
    }
}
