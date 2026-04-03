package org.kkaemok.votesystem.core;

import java.util.List;

public record ReminderConfig(
        boolean joinEnabled,
        long joinDelaySeconds,
        long joinCooldownSeconds,
        boolean scheduledEnabled,
        String scheduledTimezone,
        List<String> scheduledTimes,
        boolean intervalEnabled,
        long intervalMinutes,
        long intervalInitialDelayMinutes,
        List<String> messageLines,
        String broadcastPermission,
        ReminderSoundConfig sound
) {
    public ReminderConfig {
        joinDelaySeconds = Math.max(0L, joinDelaySeconds);
        joinCooldownSeconds = Math.max(0L, joinCooldownSeconds);
        scheduledTimezone = scheduledTimezone == null ? "system" : scheduledTimezone.trim();
        scheduledTimes = scheduledTimes == null ? List.of() : List.copyOf(scheduledTimes);
        intervalMinutes = Math.max(0L, intervalMinutes);
        intervalInitialDelayMinutes = Math.max(0L, intervalInitialDelayMinutes);
        messageLines = messageLines == null ? List.of() : List.copyOf(messageLines);
        broadcastPermission = broadcastPermission == null ? "" : broadcastPermission.trim();
        sound = sound == null ? ReminderSoundConfig.defaults() : sound;
    }

    public static ReminderConfig defaults() {
        return new ReminderConfig(
                true,
                2L,
                0L,
                false,
                "system",
                List.of("12:00", "20:00"),
                false,
                60L,
                5L,
                List.of("&6[추천] &f서버가 마음에 드셨다면 추천 부탁드려요!", "&e추천 링크: &b{link}"),
                "",
                ReminderSoundConfig.defaults()
        );
    }
}
