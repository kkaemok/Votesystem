package org.kkaemok.votesystem.velocity;

import org.kkaemok.votesystem.core.VoteConfig;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

public final class VoteSystemVelocityCommand implements SimpleCommand {
    private final VoteSystemVelocityPlugin plugin;

    public VoteSystemVelocityCommand(VoteSystemVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    @SuppressWarnings("unused")
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            source.sendMessage(plugin.text("Usage: /votesystem reload"));
            return;
        }

        VoteConfig config = plugin.getVoteConfig();
        if (!source.hasPermission(config.commandPermission())) {
            source.sendMessage(plugin.text("You do not have permission."));
            return;
        }

        plugin.reloadAll();
        source.sendMessage(plugin.text("VoteSystem reloaded."));
    }

    @Override
    @SuppressWarnings("unused")
    public boolean hasPermission(Invocation invocation) {
        VoteConfig config = plugin.getVoteConfig();
        return invocation.source().hasPermission(config.commandPermission());
    }
}
