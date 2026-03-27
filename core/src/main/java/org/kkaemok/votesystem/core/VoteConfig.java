package org.kkaemok.votesystem.core;

public record VoteConfig(
        String rewardCommand,
        boolean messageEnabled,
        String messageVoted,
        String dataStorage,
        String commandPermission,
        String recommendLink
) {
    public static VoteConfig defaults() {
        return new VoteConfig(
                "레능 코인 {player} 150",
                true,
                "추천 감사합니다! 150 코인이 지급되었습니다.",
                "data.yml",
                "votesystem.reload",
                "https://example.com/vote"
        );
    }
}
