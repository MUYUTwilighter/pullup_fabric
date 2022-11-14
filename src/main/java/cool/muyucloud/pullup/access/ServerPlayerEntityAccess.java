package cool.muyucloud.pullup.access;

import cool.muyucloud.pullup.util.PullupPlayerMovePacketC2S;

public interface ServerPlayerEntityAccess {
    double getDistanceAhead();

    double getDistancePitched10();

    double getDistancePitchedM10();

    double getRelativeHeight();

    void receivePullupPacket(PullupPlayerMovePacketC2S pullupPacket);
}
