package cool.muyucloud.pullup.util;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class PullupPlayerMovePacketC2S extends PlayerMoveC2SPacket {
    private final double distanceAhead;
    private final double relativeHeight;
    private final double distancePitched10;
    private final double distancePitchedM10;

    public PullupPlayerMovePacketC2S(double distanceAhead, double relativeHeight, double distancePitched10, double distancePitchedM10) {
        super(0, 0, 0, 0, 0, false, true, true);
        this.distanceAhead = distanceAhead;
        this.relativeHeight = relativeHeight;
        this.distancePitched10 = distancePitched10;
        this.distancePitchedM10 = distancePitchedM10;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeDouble(this.distanceAhead);
        buf.writeDouble(this.relativeHeight);
        buf.writeDouble(this.distancePitched10);
        buf.writeDouble(this.distancePitchedM10);
    }

    public double getDistanceAhead() {
        return this.distanceAhead;
    }

    public double getRelativeHeight() {
        return this.relativeHeight;
    }

    public double getDistancePitched10() {
        return this.distancePitched10;
    }

    public double getDistancePitchedM10() {
        return this.distancePitchedM10;
    }
}
