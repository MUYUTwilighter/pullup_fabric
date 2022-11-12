package cool.muyucloud.pullup.mixin;

import com.mojang.authlib.GameProfile;
import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.access.ServerPlayerEntityAccess;
import cool.muyucloud.pullup.util.Condition;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.PullupPlayerMovePacketC2S;
import cool.muyucloud.pullup.util.Registry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.HashSet;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityAccess {
    @Shadow public ServerPlayNetworkHandler networkHandler;
    private static final Collection<Condition> CONDITIONS = Registry.CONDITIONS.getAll();
    private static final Config CONFIG = Pullup.getConfig();

    private int tick = 0;
    private double distanceAhead = 500;
    private double relativeHeight = 500;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (this.world.isClient() || !CONFIG.getAsBool("enable") || !this.isFallFlying()) {
            return;
        }

        for (Condition condition : CONDITIONS) {
            if (condition.matchCondition((((ServerPlayerEntity) (Object) this)), this.world, this.tick)) {
                this.networkHandler.sendPacket(new PlaySoundIdS2CPacket(
                    condition.getSound(), SoundCategory.AMBIENT, this.getPos(), 1.0F, 1.0F));
            }
        }

        ++this.tick;
    }

    /**
     * Duck-type of ServerPlayerEntity,
     * See ServerPlayerEntityAccess::getDistanceAhead .
     * */
    public double getDistanceAhead() {
        return this.distanceAhead;
    }

    /**
     * Duck-type of ServerPlayerEntity,
     * See ServerPlayerEntityAccess::getRelativeHeight.
     * */
    public double getRelativeHeight() {
        return this.relativeHeight;
    }


    /**
     * Duck-type of ServerPlayerEntity,
     * See ServerPlayerEntityAccess::receivePullupPacket.
     * */
    public void receivePullupPacket(PullupPlayerMovePacketC2S pullupPacket) {
        this.distanceAhead = pullupPacket.getDistanceAhead();
        this.relativeHeight = pullupPacket.getRelativeHeight();
    }
}
