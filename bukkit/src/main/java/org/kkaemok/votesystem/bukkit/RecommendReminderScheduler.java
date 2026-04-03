package org.kkaemok.votesystem.bukkit;

import org.kkaemok.votesystem.core.ReminderConfig;
import org.kkaemok.votesystem.core.VoteConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RecommendReminderScheduler {
    private static final long TICKS_PER_SECOND = 20L;
    private static final long TICKS_PER_DAY = 24L * 60L * 60L * TICKS_PER_SECOND;
    private static final List<DateTimeFormatter> TIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("H:mm:ss")
    );

    private final VoteSystemBukkitPlugin plugin;
    private final List<BukkitTask> repeatingTasks = new ArrayList<>();
    private final Map<UUID, Long> joinReminderSentAt = new ConcurrentHashMap<>();

    public RecommendReminderScheduler(VoteSystemBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    public synchronized void reloadTasks() {
        cancelRepeatingTasks();
        scheduleFixedTimeReminders();
        scheduleIntervalReminder();
    }

    public synchronized void shutdown() {
        cancelRepeatingTasks();
        joinReminderSentAt.clear();
    }

    public void handlePlayerJoin(Player player) {
        VoteConfig config = plugin.getVoteConfig();
        ReminderConfig reminder = config.reminderConfig();
        if (!reminder.joinEnabled()) {
            return;
        }
        long delayTicks = secondsToTicks(reminder.joinDelaySeconds());
        Bukkit.getScheduler().runTaskLater(plugin, () -> sendJoinReminder(player.getUniqueId()), delayTicks);
    }

    private void sendJoinReminder(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            return;
        }

        ReminderConfig reminder = plugin.getVoteConfig().reminderConfig();
        if (!isJoinCooldownSatisfied(playerId, reminder.joinCooldownSeconds())) {
            return;
        }

        plugin.sendRecommendReminder(player);
        joinReminderSentAt.put(playerId, System.currentTimeMillis());
    }

    private boolean isJoinCooldownSatisfied(UUID playerId, long cooldownSeconds) {
        if (cooldownSeconds <= 0L) {
            return true;
        }

        Long lastSentAt = joinReminderSentAt.get(playerId);
        if (lastSentAt == null) {
            return true;
        }

        long elapsedMillis = System.currentTimeMillis() - lastSentAt;
        return elapsedMillis >= cooldownSeconds * 1000L;
    }

    private synchronized void cancelRepeatingTasks() {
        for (BukkitTask task : repeatingTasks) {
            if (task != null) {
                task.cancel();
            }
        }
        repeatingTasks.clear();
    }

    private void scheduleFixedTimeReminders() {
        ReminderConfig reminder = plugin.getVoteConfig().reminderConfig();
        if (!reminder.scheduledEnabled()) {
            return;
        }

        ZoneId zoneId = resolveZone(reminder.scheduledTimezone());
        Set<LocalTime> times = parseScheduledTimes(reminder.scheduledTimes());
        if (times.isEmpty()) {
            plugin.getLogger().warning("Reminder scheduled times are enabled but no valid time was found.");
            return;
        }

        for (LocalTime time : times) {
            long initialDelay = ticksUntilNextTime(time, zoneId);
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(
                    plugin,
                    plugin::broadcastRecommendReminder,
                    initialDelay,
                    TICKS_PER_DAY
            );
            repeatingTasks.add(task);
        }
    }

    private void scheduleIntervalReminder() {
        ReminderConfig reminder = plugin.getVoteConfig().reminderConfig();
        if (!reminder.intervalEnabled()) {
            return;
        }

        long periodTicks = minutesToTicks(reminder.intervalMinutes());
        if (periodTicks <= 0L) {
            plugin.getLogger().warning("Reminder interval is enabled but period-minutes is <= 0.");
            return;
        }

        long initialDelayTicks = minutesToTicks(reminder.intervalInitialDelayMinutes());
        if (initialDelayTicks <= 0L) {
            initialDelayTicks = periodTicks;
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(
                plugin,
                plugin::broadcastRecommendReminder,
                initialDelayTicks,
                periodTicks
        );
        repeatingTasks.add(task);
    }

    private ZoneId resolveZone(String configuredZone) {
        String zoneText = configuredZone == null ? "" : configuredZone.trim();
        if (zoneText.isEmpty() || zoneText.equalsIgnoreCase("system")) {
            return ZoneId.systemDefault();
        }

        try {
            return ZoneId.of(zoneText);
        } catch (DateTimeException ex) {
            plugin.getLogger().warning("Invalid reminder timezone in config.yml: " + zoneText
                    + " (fallback: system default)");
            return ZoneId.systemDefault();
        }
    }

    private Set<LocalTime> parseScheduledTimes(List<String> configuredTimes) {
        Set<LocalTime> times = new LinkedHashSet<>();
        for (String raw : configuredTimes) {
            String value = raw == null ? "" : raw.trim();
            if (value.isEmpty()) {
                continue;
            }

            Optional<LocalTime> parsed = parseTime(value);
            if (parsed.isPresent()) {
                times.add(parsed.get());
            } else {
                plugin.getLogger().warning("Invalid reminder time format in config.yml: " + value
                        + " (allowed: HH:mm or HH:mm:ss)");
            }
        }
        return times;
    }

    private Optional<LocalTime> parseTime(String value) {
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                return Optional.of(LocalTime.parse(value, formatter));
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        return Optional.empty();
    }

    private long ticksUntilNextTime(LocalTime target, ZoneId zoneId) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime next = now.with(target).withNano(0);
        if (!next.isAfter(now)) {
            next = next.plusDays(1);
        }

        long seconds = Duration.between(now, next).getSeconds();
        return Math.max(TICKS_PER_SECOND, secondsToTicks(seconds));
    }

    private static long secondsToTicks(long seconds) {
        if (seconds <= 0L) {
            return 1L;
        }
        return safeMultiply(seconds, TICKS_PER_SECOND);
    }

    private static long minutesToTicks(long minutes) {
        if (minutes <= 0L) {
            return 0L;
        }
        return safeMultiply(minutes, 60L * TICKS_PER_SECOND);
    }

    private static long safeMultiply(long a, long b) {
        try {
            return Math.multiplyExact(a, b);
        } catch (ArithmeticException ex) {
            return Long.MAX_VALUE;
        }
    }
}
