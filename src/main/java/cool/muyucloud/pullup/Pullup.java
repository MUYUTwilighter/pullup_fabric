package cool.muyucloud.pullup;

import cool.muyucloud.pullup.util.Command;
import cool.muyucloud.pullup.util.ConditionLoader;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.Registry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class Pullup implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Config CONFIG = new Config();

    @Override
    public void onInitialize() {
        LOGGER.info("Loading config.");
        CONFIG.loadAndCorrect();

        LOGGER.info("Registering arguments.");
        Registry.registerArguments();

        LOGGER.info("Registering events.");
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

        LOGGER.info("Registering commands.");
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> Command.register(dispatcher));

        LOGGER.info("Initializing condition set.");
        String loadSet = CONFIG.getAsString("loadSet");
        if (Objects.equals(loadSet, "default")) {
            ConditionLoader.loadDefault();
        }
        ConditionLoader.load(loadSet);
        LOGGER.info(String.format("Loaded condition set %s.", loadSet));
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
    }
}
