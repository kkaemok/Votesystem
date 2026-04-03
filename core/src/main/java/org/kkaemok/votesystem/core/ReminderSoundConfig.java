package org.kkaemok.votesystem.core;

public record ReminderSoundConfig(
        boolean enabled,
        String name,
        float volume,
        float pitch
) {
    public ReminderSoundConfig {
        name = name == null ? "" : name.trim();
        volume = Math.max(0.0f, volume);
        pitch = Math.max(0.0f, pitch);
    }

    public static ReminderSoundConfig defaults() {
        return new ReminderSoundConfig(
                true,
                "ENTITY_EXPERIENCE_ORB_PICKUP",
                1.0f,
                1.0f
        );
    }
}
