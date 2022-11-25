package cool.muyucloud.pullup;

import cool.muyucloud.pullup.util.*;
import cool.muyucloud.pullup.util.command.ClientCommand;
import cool.muyucloud.pullup.util.command.ServerCommand;
import cool.muyucloud.pullup.util.condition.ConditionLoader;
import cool.muyucloud.pullup.util.network.PullupNetworkC2S;
import cool.muyucloud.pullup.util.network.PullupNetworkS2C;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Pullup implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Config CONFIG = new Config();

    @Override
    public void onInitialize() {
        LOGGER.info("Loading config.");
        CONFIG.loadAndCorrect();

        LOGGER.info("Registering arguments.");
        Registry.registerArguments();

        LOGGER.info("Registering operators.");
        Registry.registerOperators();

        LOGGER.info("Registering events.");
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

        LOGGER.info("Registering commands.");
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> ServerCommand.register(dispatcher));
        ClientCommand.register(ClientCommandManager.DISPATCHER);

        LOGGER.info("Registering network.");
        PullupNetworkS2C.registerReceive();
        PullupNetworkC2S.registerReceive();

        LOGGER.info("Generating example condition set.");
        ConditionLoader.writeDefaultConditions();
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static Config getConfig() {
        return CONFIG;
    }

    public void onServerStopping(MinecraftServer server) {
        LOGGER.info("Dumping current config into file.");
        CONFIG.save();
        LOGGER.info("Generating example condition set.");
        ConditionLoader.writeDefaultConditions();
    }
}
