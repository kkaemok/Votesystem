package org.kkaemok.votesystem.velocity;

import org.kkaemok.votesystem.core.VoteConfig;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
            root.node("reward-command").set(defaults.rewardCommand());
            root.node("message-enabled").set(defaults.messageEnabled());
            root.node("message-voted").set(defaults.messageVoted());
            root.node("data-storage").set(defaults.dataStorage());
            root.node("command-permission").set(defaults.commandPermission());
            root.node("recommend-link").set(defaults.recommendLink());
            loader.save(root);
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(configPath).build();
        ConfigurationNode root = loader.load();

        return new VoteConfig(
                root.node("reward-command").getString(defaults.rewardCommand()),
                root.node("message-enabled").getBoolean(defaults.messageEnabled()),
                root.node("message-voted").getString(defaults.messageVoted()),
                root.node("data-storage").getString(defaults.dataStorage()),
                root.node("command-permission").getString(defaults.commandPermission()),
                root.node("recommend-link").getString(defaults.recommendLink())
        );
    }
}
