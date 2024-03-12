package cool.muyucloud.pullup.access;

import cool.muyucloud.pullup.util.condition.Condition;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

/**
 * Duck-types for ClientPlayerEntityMixin
 * To provide access when only ClientPlayerEntity is present but CLientPlayerEntityMixin is not.
 */
public interface ClientPlayerEntityAccess {
    /**
     * Get distance to a block or fluid in front of a player horizontally.
     * Ignoring the pitch.
     */
    double getPitchedDistanceAhead(float pitch);

    /**
     * Get distance to a block or fluid in front of a player horizontally.
     * Considering the pitch.
     */
    double getDistanceHorizontal();

    /**
     * Get distance from the player to the block right below.
     */
    double getRelativeHeight();

    /**
     * Get change of the yaw.
     * Compare current yaw with yaw at last client tick.
     */
    double getDeltaYaw();

    /**
     * Get change of the pitch.
     * Compare current pitch with pitch at last client tick.
     */
    double getDeltaPitch();

    /**
     * Get distance from the player to the block where player's velocity points to
     * */
    double getDistanceForward();

    /**
     * Get pullup texts to be displayed on the flight hud
     */
    List<Condition.ColoredText> getHudTexts();
}
