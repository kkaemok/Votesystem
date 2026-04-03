package org.kkaemok.votesystem.core;

import java.util.ArrayList;
import java.util.List;

public record VoteConfig(
        List<String> rewardCommands,
        boolean messageEnabled,
        String messageVoted,
        String dataStorage,
        String commandPermission,
        String recommendLink,
        ReminderConfig reminderConfig
) {
    public VoteConfig {
        List<String> normalizedCommands = new ArrayList<>();
        if (rewardCommands != null) {
            for (String command : rewardCommands) {
                if (command == null) {
                    continue;
                }
                String trimmed = command.trim();
                if (!trimmed.isBlank()) {
                    normalizedCommands.add(trimmed);
                }
            }
        }
        rewardCommands = List.copyOf(normalizedCommands);
        reminderConfig = reminderConfig == null ? ReminderConfig.defaults() : reminderConfig;
    }

    public static VoteConfig defaults() {
        return new VoteConfig(
                List.of("레능 코인 {player} 150"),
                true,
                "추천 감사합니다! 150 코인이 지급되었습니다.",
                "data.yml",
                "votesystem.reload",
                "https://example.com/vote",
                ReminderConfig.defaults()
        );
    }
}
