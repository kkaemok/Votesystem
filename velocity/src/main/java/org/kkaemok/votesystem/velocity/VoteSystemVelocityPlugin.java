package org.kkaemok.votesystem.velocity;

import org.kkaemok.votesystem.core.PlayerNameUtil;
import org.kkaemok.votesystem.core.VoteConfig;
import org.kkaemok.votesystem.core.VoteData;
import org.kkaemok.votesystem.core.VoteDataStore;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.event.EventManager;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import com.google.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

@Plugin(
        id = "votesystem",
        name = "VoteSystem",
        version = "1.0.0",
        authors = {"JEJUEDU"}
)
public final class VoteSystemVelocityPlugin {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDir;

    private VoteConfig config;
    private VoteData data;
    private VoteDataStore dataStore;

    @Inject
    public VoteSystemVelocityPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDir) {
        this.server = server;
        this.logger = logger;
        this.dataDir = dataDir;
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onProxyInitialize(ProxyInitializeEvent event) {
        reloadAll();
        registerVoteListener();
        registerCommand();
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onProxyShutdown(ProxyShutdownEvent event) {
        saveData();
    }

    public VoteConfig getVoteConfig() {
        return config;
    }

    public synchronized void reloadAll() {
        saveData();
        try {
            this.config = VelocityConfigLoader.load(dataDir);
        } catch (IOException ex) {
            logger.warn("Failed to load config.yml, using defaults", ex);
            this.config = VoteConfig.defaults();
        }
        this.dataStore = new VelocityYamlDataStore(dataDir.resolve(config.dataStorage()));
        this.data = loadData();
    }

    private VoteData loadData() {
        try {
            return dataStore.load();
        } catch (IOException ex) {
            logger.warn("Failed to load vote data, using empty data", ex);
            return new VoteData();
        }
    }

    public synchronized void saveData() {
        if (dataStore == null || data == null) {
            return;
        }
        try {
            dataStore.save(data);
        } catch (IOException ex) {
            logger.warn("Failed to save vote data", ex);
        }
    }

    private void registerCommand() {
        CommandManager commandManager = server.getCommandManager();
        CommandMeta meta = commandManager.metaBuilder("votesystem").build();
        commandManager.register(meta, new VoteSystemVelocityCommand(this));
    }

    private void registerVoteListener() {
        EventManager eventManager = server.getEventManager();
        List<String> candidates = List.of(
                "com.vexsoftware.votifier.velocity.event.VotifierEvent",
                "com.vexsoftware.votifier.velocity.event.VoteEvent",
                "com.vexsoftware.votifier.velocity.event.VoteReceivedEvent"
        );

        for (String className : candidates) {
            try {
                Class<?> eventClass = Class.forName(className);
                eventManager.register(this, eventClass, this::handleVoteEvent);
                logger.info("Registered NuVotifier listener using {}", className);
                return;
            } catch (ClassNotFoundException ignored) {
                // try next
            } catch (Throwable ex) {
                logger.warn("Failed to register NuVotifier listener for {}", className, ex);
                return;
            }
        }

        logger.warn("Could not find NuVotifier velocity event class. Vote logging on proxy is disabled.");
    }

    private void handleVoteEvent(Object event) {
        Object vote = invokeNoArg(event);
        String playerName = invokeString(vote, "getUsername", "getUser", "getUserName");
        if (playerName == null || playerName.isBlank()) {
            return;
        }
        String playerKey = PlayerNameUtil.normalizeKey(playerName);
        data.incrementTotalVotes(playerKey);
        saveData();

        String serviceName = invokeString(vote, "getServiceName", "getService");
        if (serviceName == null) {
            logger.info("Vote received for {}", playerName);
        } else {
            logger.info("Vote received for {} via {}", playerName, serviceName);
        }
    }

    private static Object invokeNoArg(Object target) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod("getVote");
            return method.invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static String invokeString(Object target, String... methodNames) {
        if (target == null) {
            return null;
        }
        for (String methodName : methodNames) {
            try {
                Method method = target.getClass().getMethod(methodName);
                Object result = method.invoke(target);
                if (result instanceof String str && !str.isBlank()) {
                    return str;
                }
            } catch (ReflectiveOperationException ignored) {
                // try next
            }
        }
        return null;
    }

    public Component text(String message) {
        return Component.text(message);
    }
}
