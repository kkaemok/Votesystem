package org.kkaemok.votesystem.bukkit;

import org.kkaemok.votesystem.core.VoteConfig;
import org.bukkit.configuration.file.FileConfiguration;

public final class BukkitConfigLoader {
    private BukkitConfigLoader() {
    }

    public static VoteConfig load(FileConfiguration configuration) {
        VoteConfig defaults = VoteConfig.defaults();
        return new VoteConfig(
                configuration.getString("reward-command", defaults.rewardCommand()),
                configuration.getBoolean("message-enabled", defaults.messageEnabled()),
                configuration.getString("message-voted", defaults.messageVoted()),
                configuration.getString("data-storage", defaults.dataStorage()),
                configuration.getString("command-permission", defaults.commandPermission()),
                configuration.getString("recommend-link", defaults.recommendLink())
        );
    }
}
