package org.kkaemok.votesystem.bukkit;

import org.kkaemok.votesystem.core.ReminderConfig;
import org.kkaemok.votesystem.core.ReminderSoundConfig;
import org.kkaemok.votesystem.core.VoteConfig;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public final class BukkitConfigLoader {
    private BukkitConfigLoader() {
    }

    public static VoteConfig load(FileConfiguration configuration) {
        VoteConfig defaults = VoteConfig.defaults();
        List<String> rewardCommands = configuration.isSet("reward-commands")
                ? configuration.getStringList("reward-commands")
                : defaults.rewardCommands();
        ReminderConfig reminderDefaults = defaults.reminderConfig();
        ReminderSoundConfig soundDefaults = reminderDefaults.sound();
        List<String> scheduledTimes = configuration.isSet("reminder.scheduled.times")
                ? configuration.getStringList("reminder.scheduled.times")
                : reminderDefaults.scheduledTimes();
        List<String> messageLines = configuration.isSet("reminder.message")
                ? configuration.getStringList("reminder.message")
                : reminderDefaults.messageLines();

        return new VoteConfig(
                rewardCommands,
                configuration.getBoolean("message-enabled", defaults.messageEnabled()),
                configuration.getString("message-voted", defaults.messageVoted()),
                configuration.getString("data-storage", defaults.dataStorage()),
                configuration.getString("command-permission", defaults.commandPermission()),
                configuration.getString("recommend-link", defaults.recommendLink()),
                new ReminderConfig(
                        configuration.getBoolean("reminder.join.enabled", reminderDefaults.joinEnabled()),
                        configuration.getLong("reminder.join.delay-seconds", reminderDefaults.joinDelaySeconds()),
                        configuration.getLong("reminder.join.cooldown-seconds", reminderDefaults.joinCooldownSeconds()),
                        configuration.getBoolean("reminder.scheduled.enabled", reminderDefaults.scheduledEnabled()),
                        configuration.getString("reminder.scheduled.timezone", reminderDefaults.scheduledTimezone()),
                        scheduledTimes,
                        configuration.getBoolean("reminder.interval.enabled", reminderDefaults.intervalEnabled()),
                        configuration.getLong("reminder.interval.period-minutes", reminderDefaults.intervalMinutes()),
                        configuration.getLong("reminder.interval.initial-delay-minutes", reminderDefaults.intervalInitialDelayMinutes()),
                        messageLines,
                        configuration.getString("reminder.broadcast-permission", reminderDefaults.broadcastPermission()),
                        new ReminderSoundConfig(
                                configuration.getBoolean("reminder.sound.enabled", soundDefaults.enabled()),
                                configuration.getString("reminder.sound.name", soundDefaults.name()),
                                (float) configuration.getDouble("reminder.sound.volume", soundDefaults.volume()),
                                (float) configuration.getDouble("reminder.sound.pitch", soundDefaults.pitch())
                        )
                )
        );
    }
}
