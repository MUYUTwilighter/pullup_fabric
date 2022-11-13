package cool.muyucloud.pullup.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.operator.Operator;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Condition {
    private final Identifier id;
    private final int checkDelay;
    private final int playDelay;
    private final boolean loopPlay;
    private final Identifier sound;
    private final HashMap<String, Identifier> arguments;
    private final HashSet<Expression> expressions;

    public Condition(Identifier id, int playDelay, boolean loopPlay, String sound, int checkDelay, Map<String, String> arguments, String... expressions) {
        this.id = id;
        this.checkDelay = checkDelay;
        this.sound = new Identifier(sound);
        this.playDelay = playDelay;
        this.loopPlay = loopPlay;
        this.arguments = new HashMap<>();
        for (String var : arguments.keySet()) {
            Identifier argId = new Identifier(arguments.get(var));
            if (Registry.ARGUMENTS.get(argId) == null) {
                throw new NullPointerException(String.format("No such argument \"%s\"", argId));
            }
            this.arguments.put(var, argId);
        }

        this.expressions = new HashSet<>();
        for (String expression : expressions) {
            this.expressions.add(parseExpression(expression));
        }
    }

    @NotNull
    private Expression parseExpression(String expression) {
        ExpressionBuilder builder = new ExpressionBuilder(expression);
        for (String var : this.arguments.keySet()) {
            builder.variable(var);
            for (Operator operator : Registry.OPERATOR.getAll()) {
                builder.operator(operator);
            }
        }
        return builder.build();
    }

    public Identifier getSound() {
        return this.sound;
    }

    public boolean verifyExpressions(ServerPlayerEntity player, World world) {
        for (Expression expression : this.expressions) {
            if (computeExpression(player, world, expression) < 0) return false;
        }
        return true;
    }

    private double computeExpression(ServerPlayerEntity player, World world, Expression expression) {
        for (String var : this.arguments.keySet()) {
            double value = Registry.ARGUMENTS.get(this.arguments.get(var)).compute(player, world);
            expression.setVariable(var, value);
        }
        return expression.evaluate();
    }

    public Identifier getId() {
        return this.id;
    }

    public int getPlayDelay() {
        return this.playDelay;
    }

    public int getCheckDelay() {
        return this.checkDelay;
    }

    public boolean shouldLoopPlay() {
        return this.loopPlay;
    }
}
