package org.kkaemok.votesystem.bukkit;

import org.kkaemok.votesystem.core.CommandTemplate;
import org.kkaemok.votesystem.core.VoteConfig;
import org.kkaemok.votesystem.core.VoteData;
import org.kkaemok.votesystem.core.VoteDataStore;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class VoteSystemBukkitPlugin extends JavaPlugin {
    private VoteConfig config;
    private VoteData data;
    private VoteDataStore dataStore;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadAll();

        getServer().getPluginManager().registerEvents(new VoteListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        PluginCommand command = getCommand("votesystem");
        if (command != null) {
            command.setExecutor(new VoteSystemCommand(this));
        }
        PluginCommand recommendCommand = getCommand("추천");
        if (recommendCommand != null) {
            recommendCommand.setExecutor(new RecommendCommand(this));
        }
    }

    @Override
    public void onDisable() {
        saveData();
    }

    public synchronized void reloadAll() {
        saveData();
        reloadConfig();
        this.config = BukkitConfigLoader.load(getConfig());
        this.dataStore = new YamlVoteDataStore(new File(getDataFolder(), config.dataStorage()));
        this.data = loadData();
    }

    public VoteConfig getVoteConfig() {
        return config;
    }

    public VoteData getVoteData() {
        return data;
    }

    public synchronized void saveData() {
        if (dataStore == null || data == null) {
            return;
        }
        try {
            dataStore.save(data);
        } catch (IOException ex) {
            getLogger().log(Level.WARNING, "Failed to save vote data", ex);
        }
    }

    private VoteData loadData() {
        try {
            return dataStore.load();
        } catch (IOException ex) {
            getLogger().log(Level.WARNING, "Failed to load vote data, using empty data", ex);
            return new VoteData();
        }
    }

    public void dispatchRewardCommand(String playerName) {
        String rawCommand = config.rewardCommand();
        if (rawCommand == null || rawCommand.isBlank()) {
            return;
        }
        String command = rawCommand.startsWith("/") ? rawCommand.substring(1) : rawCommand;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), CommandTemplate.applyPlayer(command, playerName));
    }
}
