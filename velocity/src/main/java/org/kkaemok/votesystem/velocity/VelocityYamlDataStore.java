package org.kkaemok.votesystem.velocity;

import org.kkaemok.votesystem.core.PlayerNameUtil;
import org.kkaemok.votesystem.core.VoteData;
import org.kkaemok.votesystem.core.VoteDataStore;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class VelocityYamlDataStore implements VoteDataStore {
    private final Path path;

    public VelocityYamlDataStore(Path path) {
        this.path = path;
    }

    @Override
    public VoteData load() throws IOException {
        VoteData data = new VoteData();
        if (Files.notExists(path)) {
            save(data);
            return data;
        }
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(path).build();
        ConfigurationNode root = loader.load();

        ConfigurationNode pending = root.node("pending");
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : pending.childrenMap().entrySet()) {
            String key = String.valueOf(entry.getKey());
            int amount = entry.getValue().getInt(0);
            if (amount > 0) {
                data.getPendingRewards().put(PlayerNameUtil.normalizeKey(key), amount);
            }
        }

        ConfigurationNode total = root.node("totalVotes");
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : total.childrenMap().entrySet()) {
            String key = String.valueOf(entry.getKey());
            int amount = entry.getValue().getInt(0);
            if (amount > 0) {
                data.getTotalVotes().put(PlayerNameUtil.normalizeKey(key), amount);
            }
        }

        return data;
    }

    @Override
    public void save(VoteData data) throws IOException {
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(path).build();
        ConfigurationNode root = loader.createNode();

        for (Map.Entry<String, Integer> entry : data.getPendingRewards().entrySet()) {
            root.node("pending", entry.getKey()).set(entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : data.getTotalVotes().entrySet()) {
            root.node("totalVotes", entry.getKey()).set(entry.getValue());
        }

        if (path.getParent() != null && Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        loader.save(root);
    }
}
