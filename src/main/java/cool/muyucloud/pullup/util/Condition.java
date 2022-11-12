package cool.muyucloud.pullup.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.operator.Operator;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class Condition {
    private final String name;
    private final int checkDelay;
    private final Identifier sound;
    private final HashSet<Identifier> arguments;
    private final HashSet<Expression> expressions;

    public Condition(String name, String sound, int checkDelay, String[] expressions, String... arguments) {
        this.name = name;
        this.checkDelay = checkDelay;
        this.sound = new Identifier(sound);
        this.arguments = new HashSet<>();
        for (String argument : arguments) {
            Identifier id = new Identifier(argument);
            if (Registry.ARGUMENTS.get(new Identifier(argument)) != null) {
                this.arguments.add(id);
            }
        }

        this.expressions = new HashSet<>();
        for (String expression : expressions) {
            this.expressions.add(parseExpression(expression));
        }
    }

    @NotNull
    private Expression parseExpression(String expression) {
        ExpressionBuilder builder = new ExpressionBuilder(expression);
        for (Identifier id : this.arguments) {
            String argument = String.format("%s_%s", id.getNamespace(), id.getPath());
            builder.variable(argument);
            for (Operator operator : Registry.OPERATOR.getAll()) {
                builder.operator(operator);
            }
        }
        return builder.build();
    }

    public boolean matchCondition(ServerPlayerEntity player, World world, int tick) {
        if (tick % this.checkDelay != 0) {
            return false;
        }
        return verifyExpressions(player, world);
    }

    public Identifier getSound() {
        return this.sound;
    }

    private boolean verifyExpressions(ServerPlayerEntity player, World world) {
        for (Expression expression : this.expressions) {
            if (computeExpression(player, world, expression) < 0) return false;
        }
        return true;
    }

    private double computeExpression(ServerPlayerEntity player, World world, Expression expression) {
        for (Identifier id : this.arguments) {
            double value = Registry.ARGUMENTS.get(id).compute(player, world);
            String argument = String.format("%s_%s", id.getNamespace(), id.getPath());
            expression.setVariable(argument, value);
        }
        return expression.evaluate();
    }

    public String getName() {
        return this.name;
    }
}
