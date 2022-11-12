package cool.muyucloud.pullup.mixin;

import cool.muyucloud.pullup.access.ServerPlayerEntityAccess;
import cool.muyucloud.pullup.util.PullupPlayerMovePacketC2S;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onPlayerMove", at = @At("HEAD"), cancellable = true)
    public void onPlayerMove(PlayerMoveC2SPacket packet, CallbackInfo ci) {
        if (packet instanceof PullupPlayerMovePacketC2S pullupPacket) {
            ((ServerPlayerEntityAccess) this.player).receivePullupPacket(pullupPacket);
            ci.cancel();
        }
    }
}
