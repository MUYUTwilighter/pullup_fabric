package cool.muyucloud.pullup.access;

/**
 * Duck-types for ClientPlayerEntityMixin
 * To provide access when only ClientPlayerEntity is present but CLientPlayerEntityMixin is not.
 * */
public interface ClientPlayerEntityAccess {
    /**
     * Get distance to a block or fluid in front of a player horizontally.
     * Ignoring the pitch.
     * */
    double getPitchedDistanceAhead(float pitch);

    /**
     * Get distance to a block or fluid in front of a player horizontally.
     * Considering the pitch.
     * */
    double getDistanceHorizontal();

    /**
     * Get distance from the player to the block right below.
     */
    double getRelativeHeight();

    /**
     * Get change of the yaw.
     * Compare current yaw with yaw at last client tick.
     * */
    double getDeltaYaw();

    /**
     * Get change of the pitch.
     * Compare current pitch with pitch at last client tick.
     * */
    double getDeltaPitch();

    /**
     * Get ticks since last beginning of flight.
     * 1 tick = 50 ms
     * */
    double getFlightTicks();
}
