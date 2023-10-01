package cool.muyucloud.pullup.util.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.condition.ConditionLoader;
import cool.muyucloud.pullup.util.network.PullupNetworkS2C;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class ServerCommand {
    private static final SuggestionProvider<ServerCommandSource> CONDITION_SETS = (context, builder) -> CommandSource.suggestMatching(ConditionLoader.getFileList(), builder);
    private static final Config CONFIG = Pullup.getConfig();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal("pullupServer")
            .requires(source -> source.hasPermissionLevel(2));

        root.then(buildLoad());
        root.then(CommandManager.literal("enableSend").executes(ServerCommand::enableSend));
        root.then(CommandManager.literal("disableSend").executes(ServerCommand::disableSend));

        dispatcher.register(root);
    }

    private static int enableSend(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        MutableText text = Text.translatable("command.pullup.server.enableSend");
        source.sendFeedback(() -> text, true);
        CONFIG.set("sendServer", true);
        return 1;
    }

    private static int disableSend(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        MutableText text = Text.translatable("command.pullup.server.disableSend");
        source.sendFeedback(() -> text, true);
        CONFIG.set("sendServer", false);
        return 1;
    }

    private static LiteralArgumentBuilder<ServerCommandSource> buildLoad() {
        LiteralArgumentBuilder<ServerCommandSource> conditionSet = CommandManager.literal("load");

        conditionSet.then(
            CommandManager.argument("setName", StringArgumentType.string()).suggests(CONDITION_SETS)
                .executes(context -> loadSet(StringArgumentType.getString(context, "setName"), context.getSource()))
        );
        conditionSet.then(
            CommandManager.literal("default").executes(context -> loadDefault(context.getSource()))
        );

        return conditionSet;
    }

    private static int loadSet(String name, ServerCommandSource source) {
        if (!ConditionLoader.containsFile(name)) {
            source.sendFeedback(() -> Text.translatable("command.pullup.client.load.specific.notExist"), false);
            return 0;
        }

        MutableText text = Text.translatable("command.pullup.client.load.specific.loading");
        source.sendFeedback(() -> text, true);
        CONFIG.set("loadSet", name);
        if (CONFIG.getAsBool("sendServer")) {
            PullupNetworkS2C.sendClear(source.getPlayer());
            PullupNetworkS2C.sendLoad(source.getPlayer());
        }
        return 1;
    }

    private static int loadDefault(ServerCommandSource source) {
        MutableText text = Text.translatable("command.pullup.client.load.default");
        source.sendFeedback(() -> text, true);
        CONFIG.set("loadSet", "default");
        if (CONFIG.getAsBool("sendServer")) {
            PullupNetworkS2C.sendClear(source.getPlayer());
            PullupNetworkS2C.sendLoad(source.getPlayer());
        }
        return 1;
    }
}
