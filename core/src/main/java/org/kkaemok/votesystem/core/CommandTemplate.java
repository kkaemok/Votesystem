package org.kkaemok.votesystem.core;

public final class CommandTemplate {
    private CommandTemplate() {
    }

    public static String applyPlayer(String template, String playerName) {
        if (template == null) {
            return "";
        }
        return template.replace("{player}", playerName);
    }
}
