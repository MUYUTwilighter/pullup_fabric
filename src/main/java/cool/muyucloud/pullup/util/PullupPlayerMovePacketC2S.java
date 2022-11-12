package cool.muyucloud.pullup.util;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class PullupPlayerMovePacketC2S extends PlayerMoveC2SPacket {
    private final double distanceAhead;
    private final double relativeHeight;

    public PullupPlayerMovePacketC2S(double distanceAhead, double relativeHeight) {
        super(0, 0, 0, 0, 0, false, true, true);
        this.distanceAhead = distanceAhead;
        this.relativeHeight = relativeHeight;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeDouble(this.distanceAhead);
        buf.writeDouble(this.relativeHeight);
    }

    public double getDistanceAhead() {
        return this.distanceAhead;
    }

    public double getRelativeHeight() {
        return this.relativeHeight;
    }
}
