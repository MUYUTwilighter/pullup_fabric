package cool.muyucloud.pullup.mixin;

import com.mojang.authlib.GameProfile;
import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.PullupPlayerMovePacketC2S;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity {
    @Shadow @Final public ClientPlayNetworkHandler networkHandler;
    private static final Config CONFIG = Pullup.getConfig();

    public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    public void sendMovementPackets(CallbackInfo ci) {
        PullupPlayerMovePacketC2S packet = new PullupPlayerMovePacketC2S(this.getDistanceAhead(), this.getRelativeHeight());
        this.networkHandler.sendPacket(packet);
    }

    private double getDistanceAhead() {
        int maxDistance = CONFIG.getAsInt("maxDistance");
        Vec3d target = this.raycast(maxDistance, 0, false).getPos();
        return this.getPos().distanceTo(target);
    }

    private double getRelativeHeight() {
        int maxDistance = CONFIG.getAsInt("maxDistance");
        Vec3d cameraPos = this.getCameraPosVec(0);
        Vec3d endPos = cameraPos.add(0, -maxDistance, 0);
        Vec3d target = this.world.raycast(new RaycastContext(cameraPos, endPos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, this)).getPos();
        return cameraPos.distanceTo(target);
    }
}
