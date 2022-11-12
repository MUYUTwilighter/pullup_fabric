package cool.muyucloud.pullup.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import cool.muyucloud.pullup.Pullup;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

public class Command {
    private static final SuggestionProvider<ServerCommandSource> CONDITION_SETS = (context, builder) -> {
        String[] files = ConditionLoader.getFiles();
        return CommandSource.suggestMatching(files, builder);
    };
    private static final Config CONFIG = Pullup.getConfig();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal("pullup")
            .requires(source -> source.hasPermissionLevel(2));

        root.then(buildLoad());

        root.then(CommandManager.literal("enable").executes(Command::executeEnable));
        root.then(CommandManager.literal("enable").executes(Command::executeDisable));

        dispatcher.register(root);
    }

    private static int executeEnable(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        MutableText text = new LiteralText("Enable Pullup.");
        source.sendFeedback(text, true);
        CONFIG.set("enable", true);
        return 1;
    }

    private static int executeDisable(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        MutableText text = new LiteralText("Disable Pullup.");
        source.sendFeedback(text, false);
        CONFIG.set("enable", true);
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
        MutableText text = new LiteralText(String.format("正在加载条件组 %s.", name));
        source.sendFeedback(text, true);
        ConditionLoader.load(name);
        return 1;
    }

    private static int loadDefault(ServerCommandSource source) {
        MutableText text = new LiteralText("正在加载默认条件组");
        source.sendFeedback(text, true);
        ConditionLoader.loadDefault();
        return 1;
    }
}
