package cool.muyucloud.pullup.util;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.world.World;

public interface Argument {
    double compute(ClientPlayerEntity player, World world);
}
