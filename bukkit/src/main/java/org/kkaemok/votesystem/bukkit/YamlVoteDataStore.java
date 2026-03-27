package org.kkaemok.votesystem.bukkit;

import org.kkaemok.votesystem.core.PlayerNameUtil;
import org.kkaemok.votesystem.core.VoteData;
import org.kkaemok.votesystem.core.VoteDataStore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public final class YamlVoteDataStore implements VoteDataStore {
    private final File file;

    public YamlVoteDataStore(File file) {
        this.file = file;
    }

    @Override
    public VoteData load() throws IOException {
        VoteData data = new VoteData();
        if (!file.exists()) {
            save(data);
            return data;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection pending = yaml.getConfigurationSection("pending");
        if (pending != null) {
            for (String key : pending.getKeys(false)) {
                int amount = pending.getInt(key, 0);
                if (amount > 0) {
                    data.getPendingRewards().put(PlayerNameUtil.normalizeKey(key), amount);
                }
            }
        }

        ConfigurationSection total = yaml.getConfigurationSection("totalVotes");
        if (total != null) {
            for (String key : total.getKeys(false)) {
                int amount = total.getInt(key, 0);
                if (amount > 0) {
                    data.getTotalVotes().put(PlayerNameUtil.normalizeKey(key), amount);
                }
            }
        }

        return data;
    }

    @Override
    public void save(VoteData data) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<String, Integer> entry : data.getPendingRewards().entrySet()) {
            yaml.set("pending." + entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : data.getTotalVotes().entrySet()) {
            yaml.set("totalVotes." + entry.getKey(), entry.getValue());
        }

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("Failed to create data directory: " + parent);
            }
        }
        yaml.save(file);
    }
}
