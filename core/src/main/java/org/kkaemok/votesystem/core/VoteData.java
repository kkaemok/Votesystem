package org.kkaemok.votesystem.core;

import java.util.HashMap;
import java.util.Map;

public final class VoteData {
    private final Map<String, Integer> pendingRewards = new HashMap<>();
    private final Map<String, Integer> totalVotes = new HashMap<>();

    public Map<String, Integer> getPendingRewards() {
        return pendingRewards;
    }

    public Map<String, Integer> getTotalVotes() {
        return totalVotes;
    }

    public int getPending(String playerKey) {
        return pendingRewards.getOrDefault(playerKey, 0);
    }

    public void addPending(String playerKey, int amount) {
        if (amount <= 0) {
            return;
        }
        pendingRewards.put(playerKey, getPending(playerKey) + amount);
    }

    public void clearPending(String playerKey) {
        pendingRewards.remove(playerKey);
    }

    public void incrementTotalVotes(String playerKey) {
        totalVotes.put(playerKey, totalVotes.getOrDefault(playerKey, 0) + 1);
    }
}
