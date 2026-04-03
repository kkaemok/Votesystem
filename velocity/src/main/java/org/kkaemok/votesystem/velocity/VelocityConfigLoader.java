package org.kkaemok.votesystem.velocity;

import org.kkaemok.votesystem.core.ReminderConfig;
import org.kkaemok.votesystem.core.ReminderSoundConfig;
import org.kkaemok.votesystem.core.VoteConfig;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class VelocityConfigLoader {
    private VelocityConfigLoader() {
    }

    public static VoteConfig load(Path dataDir) throws IOException {
        VoteConfig defaults = VoteConfig.defaults();
        Path configPath = dataDir.resolve("config.yml");

        if (Files.notExists(configPath)) {
            Files.createDirectories(dataDir);
            YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(configPath).build();
            ConfigurationNode root = loader.createNode();
            writeDefaults(root, defaults);
            loader.save(root);
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(configPath).build();
        ConfigurationNode root = loader.load();
        ConfigurationNode rewardCommandsNode = root.node("reward-commands");
        List<String> rewardCommands = rewardCommandsNode.virtual()
                ? defaults.rewardCommands()
                : rewardCommandsNode.getList(String.class, List.of());
        ReminderConfig reminderDefaults = defaults.reminderConfig();
        ReminderSoundConfig soundDefaults = reminderDefaults.sound();

        return new VoteConfig(
                rewardCommands,
                root.node("message-enabled").getBoolean(defaults.messageEnabled()),
                root.node("message-voted").getString(defaults.messageVoted()),
                root.node("data-storage").getString(defaults.dataStorage()),
                root.node("command-permission").getString(defaults.commandPermission()),
                root.node("recommend-link").getString(defaults.recommendLink()),
                new ReminderConfig(
                        root.node("reminder", "join", "enabled").getBoolean(reminderDefaults.joinEnabled()),
                        root.node("reminder", "join", "delay-seconds").getLong(reminderDefaults.joinDelaySeconds()),
                        root.node("reminder", "join", "cooldown-seconds").getLong(reminderDefaults.joinCooldownSeconds()),
                        root.node("reminder", "scheduled", "enabled").getBoolean(reminderDefaults.scheduledEnabled()),
                        root.node("reminder", "scheduled", "timezone").getString(reminderDefaults.scheduledTimezone()),
                        root.node("reminder", "scheduled", "times").getList(String.class, reminderDefaults.scheduledTimes()),
                        root.node("reminder", "interval", "enabled").getBoolean(reminderDefaults.intervalEnabled()),
                        root.node("reminder", "interval", "period-minutes").getLong(reminderDefaults.intervalMinutes()),
                        root.node("reminder", "interval", "initial-delay-minutes").getLong(reminderDefaults.intervalInitialDelayMinutes()),
                        root.node("reminder", "message").getList(String.class, reminderDefaults.messageLines()),
                        root.node("reminder", "broadcast-permission").getString(reminderDefaults.broadcastPermission()),
                        new ReminderSoundConfig(
                                root.node("reminder", "sound", "enabled").getBoolean(soundDefaults.enabled()),
                                root.node("reminder", "sound", "name").getString(soundDefaults.name()),
                                (float) root.node("reminder", "sound", "volume").getDouble(soundDefaults.volume()),
                                (float) root.node("reminder", "sound", "pitch").getDouble(soundDefaults.pitch())
                        )
                )
        );
    }

    private static void writeDefaults(ConfigurationNode root, VoteConfig defaults) throws IOException {
        root.node("reward-commands").setList(String.class, defaults.rewardCommands());
        root.node("message-enabled").set(defaults.messageEnabled());
        root.node("message-voted").set(defaults.messageVoted());
        root.node("data-storage").set(defaults.dataStorage());
        root.node("command-permission").set(defaults.commandPermission());
        root.node("recommend-link").set(defaults.recommendLink());

        ReminderConfig reminder = defaults.reminderConfig();
        root.node("reminder", "join", "enabled").set(reminder.joinEnabled());
        root.node("reminder", "join", "delay-seconds").set(reminder.joinDelaySeconds());
        root.node("reminder", "join", "cooldown-seconds").set(reminder.joinCooldownSeconds());
        root.node("reminder", "scheduled", "enabled").set(reminder.scheduledEnabled());
        root.node("reminder", "scheduled", "timezone").set(reminder.scheduledTimezone());
        root.node("reminder", "scheduled", "times").setList(String.class, reminder.scheduledTimes());
        root.node("reminder", "interval", "enabled").set(reminder.intervalEnabled());
        root.node("reminder", "interval", "period-minutes").set(reminder.intervalMinutes());
        root.node("reminder", "interval", "initial-delay-minutes").set(reminder.intervalInitialDelayMinutes());
        root.node("reminder", "message").setList(String.class, reminder.messageLines());
        root.node("reminder", "broadcast-permission").set(reminder.broadcastPermission());

        ReminderSoundConfig sound = reminder.sound();
        root.node("reminder", "sound", "enabled").set(sound.enabled());
        root.node("reminder", "sound", "name").set(sound.name());
        root.node("reminder", "sound", "volume").set(sound.volume());
        root.node("reminder", "sound", "pitch").set(sound.pitch());
    }
}
