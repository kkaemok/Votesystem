package org.kkaemok.votesystem.bukkit;

import org.kkaemok.votesystem.core.CommandTemplate;
import org.kkaemok.votesystem.core.ReminderConfig;
import org.kkaemok.votesystem.core.ReminderSoundConfig;
import org.kkaemok.votesystem.core.VoteConfig;
import org.kkaemok.votesystem.core.VoteData;
import org.kkaemok.votesystem.core.VoteDataStore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public final class VoteSystemBukkitPlugin extends JavaPlugin {
    private VoteConfig config;
    private VoteData data;
    private VoteDataStore dataStore;
    private RecommendReminderScheduler reminderScheduler;
    private String invalidReminderSound;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.reminderScheduler = new RecommendReminderScheduler(this);
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
        if (reminderScheduler != null) {
            reminderScheduler.shutdown();
        }
        saveData();
    }

    public synchronized void reloadAll() {
        saveData();
        reloadConfig();
        this.config = BukkitConfigLoader.load(getConfig());
        this.dataStore = new YamlVoteDataStore(new File(getDataFolder(), config.dataStorage()));
        this.data = loadData();
        this.invalidReminderSound = null;
        if (reminderScheduler != null) {
            reminderScheduler.reloadTasks();
        }
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

    public void dispatchRewardCommands(String playerName) {
        for (String rawCommand : config.rewardCommands()) {
            if (rawCommand == null || rawCommand.isBlank()) {
                continue;
            }
            String command = rawCommand.startsWith("/") ? rawCommand.substring(1) : rawCommand;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), CommandTemplate.applyPlayer(command, playerName));
        }
    }

    public void handleJoinReminder(Player player) {
        if (reminderScheduler == null) {
            return;
        }
        reminderScheduler.handlePlayerJoin(player);
    }

    public void broadcastRecommendReminder() {
        ReminderConfig reminder = config.reminderConfig();
        String requiredPermission = reminder.broadcastPermission();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (requiredPermission != null && !requiredPermission.isBlank() && !player.hasPermission(requiredPermission)) {
                continue;
            }
            sendRecommendReminder(player);
        }
    }

    public void sendRecommendReminder(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        ReminderConfig reminder = config.reminderConfig();
        String link = config.recommendLink();
        List<String> lines = reminder.messageLines();

        if (lines.isEmpty()) {
            String fallback = ChatColor.translateAlternateColorCodes('&', "&e추천 링크: &b" + (link == null ? "" : link));
            player.sendMessage(fallback);
        } else {
            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                String replaced = CommandTemplate.applyPlayer(line, player.getName())
                        .replace("{link}", link == null ? "" : link);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', replaced));
            }
        }

        playReminderSound(player, reminder.sound());
    }

    private void playReminderSound(Player player, ReminderSoundConfig soundConfig) {
        if (!soundConfig.enabled()) {
            return;
        }
        String name = soundConfig.name();
        if (name == null || name.isBlank()) {
            return;
        }

        try {
            Sound sound = Sound.valueOf(name.trim().toUpperCase(Locale.ROOT));
            player.playSound(player.getLocation(), sound, soundConfig.volume(), soundConfig.pitch());
            invalidReminderSound = null;
        } catch (IllegalArgumentException ex) {
            if (invalidReminderSound == null || !invalidReminderSound.equalsIgnoreCase(name)) {
                getLogger().log(Level.WARNING, "Invalid reminder sound in config.yml: " + name);
                invalidReminderSound = name;
            }
        }
    }
}
