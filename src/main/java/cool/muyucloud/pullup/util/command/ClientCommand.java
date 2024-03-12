package cool.muyucloud.pullup.util.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.Registry;
import cool.muyucloud.pullup.util.condition.ConditionLoader;
import cool.muyucloud.pullup.util.network.PullupNetworkC2S;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Logger;

public class ClientCommand {
    private static final SuggestionProvider<FabricClientCommandSource> CONDITION_SETS =
        (context, builder) -> CommandSource.suggestMatching(ConditionLoader.getFileList(), builder);
    private static final Config CONFIG = Pullup.getConfig();
    private static final Logger LOGGER = Pullup.getLogger();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> root = ClientCommandManager.literal("pullupClient");

        root.then(buildLoad());
        root.then(ClientCommandManager.literal("enable").executes(ClientCommand::executeEnable));
        root.then(ClientCommandManager.literal("disable").executes(ClientCommand::executeDisable));
        root.then(ClientCommandManager.literal("grab").executes(ClientCommand::grabConditions));
        root.then(ClientCommandManager.literal("enableServer").executes(ClientCommand::enableServer));
        root.then(ClientCommandManager.literal("enableServer").executes(ClientCommand::disableServer));

        dispatcher.register(root);
    }

    private static int executeEnable(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        MutableText text = Text.translatable("command.pullup.client.enable");
        source.sendFeedback(text);
        CONFIG.set("enable", true);
        return 1;
    }

    private static int executeDisable(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        MutableText text = Text.translatable("command.pullup.client.disable");
        source.sendFeedback(text);
        CONFIG.set("enable", true);
        return 1;
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildLoad() {
        LiteralArgumentBuilder<FabricClientCommandSource> conditionSet = ClientCommandManager.literal("load");

        conditionSet.then(
            ClientCommandManager.argument("setName", StringArgumentType.string()).suggests(CONDITION_SETS)
                .executes(context -> loadSet(StringArgumentType.getString(context, "setName"), context.getSource()))
        );
        conditionSet.then(
            ClientCommandManager.literal("default").executes(context -> loadDefault(context.getSource()))
        );

        return conditionSet;
    }

    private static int loadSet(String name, FabricClientCommandSource source) {
        if (!ConditionLoader.containsFile(name)) {
            source.sendError(Text.translatable("command.pullup.client.load.specific.notExist", name));
            return 0;
        }

        MutableText text = Text.translatable("command.pullup.client.load.specific.loading", name);
        source.sendFeedback(text);
        CONFIG.set("loadSet", name);
        Registry.CONDITIONS.clear();
        try {
            new ConditionLoader(name).load();
        } catch (Exception e) {
            LOGGER.error(String.format("Failed to load condition set %s.", name));
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    private static int loadDefault(FabricClientCommandSource source) {
        MutableText text = Text.translatable("command.pullup.client.load.default");
        source.sendFeedback(text);
        CONFIG.set("loadSet", "default");
        Registry.CONDITIONS.clear();
        new ConditionLoader().load();
        return 1;
    }

    private static int grabConditions(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();

        if (!CONFIG.getAsBool("loadServer")) {
            source.sendError(Text.translatable("command.pullup.client.grab.enableLoadServer"));
            return 0;
        }

        PullupNetworkC2S.sendGrab();
        source.sendFeedback(Text.translatable("command.pullup.client.grab.sent"));
        return 1;
    }

    private static int enableServer(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        MutableText text = Text.translatable("command.pullup.client.loadServer.enable");
        source.sendFeedback(text);
        CONFIG.set("loadServer", true);
        return 1;
    }

    private static int disableServer(CommandContext<FabricClientCommandSource> context) {
        FabricClientCommandSource source = context.getSource();
        MutableText text = Text.translatable("command.pullup.client.loadServer.disable");
        source.sendFeedback(text);
        CONFIG.set("loadServer", false);
        return 1;
    }
}
