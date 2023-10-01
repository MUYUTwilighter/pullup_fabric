package cool.muyucloud.pullup.mixin;

import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.network.PullupNetworkS2C;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Unique
    private static final Config CONFIG = Pullup.getConfig();

    @Inject(method = "onPlayerConnected", at = @At("TAIL"))
    public void onPlayerConnected(ServerPlayerEntity player, CallbackInfo ci) {
        if (CONFIG.getAsBool("sendServer")) {
            PullupNetworkS2C.sendClear(player);
            PullupNetworkS2C.sendLoad(player);
        }
    }
}
