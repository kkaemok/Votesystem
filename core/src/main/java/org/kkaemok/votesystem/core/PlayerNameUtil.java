package org.kkaemok.votesystem.core;

import java.util.Locale;

public final class PlayerNameUtil {
    private PlayerNameUtil() {
    }

    public static String normalizeKey(String playerName) {
        if (playerName == null) {
            return "";
        }
        return playerName.trim().toLowerCase(Locale.ROOT);
    }
}
