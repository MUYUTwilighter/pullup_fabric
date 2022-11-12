package cool.muyucloud.pullup.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public interface Argument {
    double compute(ServerPlayerEntity player, World world);
}
