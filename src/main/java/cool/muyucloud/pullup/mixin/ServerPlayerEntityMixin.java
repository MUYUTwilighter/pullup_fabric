package cool.muyucloud.pullup.mixin;

import com.mojang.authlib.GameProfile;
import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.access.ServerPlayerEntityAccess;
import cool.muyucloud.pullup.util.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.network.packet.s2c.play.StopSoundS2CPacket;
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
import java.util.HashMap;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityAccess {
    @Shadow
    public ServerPlayNetworkHandler networkHandler;
    private static final Collection<Condition> CONDITIONS = Registry.CONDITIONS.getAll();
    private static final Config CONFIG = Pullup.getConfig();

    private int tick = 0;
    private double distanceAhead = 500;
    private double distancePitched10 = 500;
    private double distancePitchedM10 = 500;
    private double relativeHeight = 500;
    private final HashMap<Identifier, ConditionTrigger> triggers = new HashMap<>();

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        if (this.world.isClient() || !CONFIG.getAsBool("enable") || !this.isFallFlying()) {
            return;
        }

        for (Condition condition : CONDITIONS) {
            int checkDelay = condition.getCheckDelay();
            if (this.tick % checkDelay != 0) {
                continue;
            }
            if (condition.verifyExpressions((((ServerPlayerEntity) (Object) this)), this.world)) {
                this.sendPlaySound(condition);
            } else {
                this.sendStopSound(condition);
            }
        }

        for (Identifier id : this.triggers.keySet()) {
            Condition condition = Registry.CONDITIONS.get(id);
            if (condition == null) {
                this.triggers.remove(id);
                continue;
            }

            ConditionTrigger trigger = triggers.get(id);
            if (trigger.isTriggered) {
                this.sendPlaySound(condition);
            }
        }

        ++this.tick;
    }

    /**
     * Duck-type of ServerPlayerEntity,
     * See ServerPlayerEntityAccess::getDistanceAhead .
     */
    public double getDistanceAhead() {
        return this.distanceAhead;
    }

    /**
     * Duck-type of ServerPlayerEntity,
     * See ServerPlayerEntityAccess::getRelativeHeight.
     */
    public double getRelativeHeight() {
        return this.relativeHeight;
    }

    /**
     * Duck-type of ServerPlayerEntity,
     * See ServerPlayerEntityAccess::getDistancePitched10.
     */
    public double getDistancePitched10() {
        return this.distancePitched10;
    }

    /**
     * Duck-type of ServerPlayerEntity,
     * See ServerPlayerEntityAccess::getDistancePitchedM10.
     */
    public double getDistancePitchedM10() {
        return this.distancePitchedM10;
    }


    /**
     * Duck-type of ServerPlayerEntity,
     * See ServerPlayerEntityAccess::receivePullupPacket.
     */
    public void receivePullupPacket(PullupPlayerMovePacketC2S pullupPacket) {
        this.distanceAhead = pullupPacket.getDistanceAhead();
        this.relativeHeight = pullupPacket.getRelativeHeight();
        this.distancePitched10 = pullupPacket.getDistancePitched10();
        this.distancePitchedM10 = pullupPacket.getDistancePitchedM10();
    }

    private void sendPlaySound(Condition condition) {
        Identifier id = condition.getId();
        if (!this.triggers.containsKey(id)) {
            ConditionTrigger trigger = new ConditionTrigger();
            trigger.isTriggered = true;
            trigger.lastPlay = this.tick;
            this.triggers.put(id, trigger);
            this.networkHandler.sendPacket(new PlaySoundIdS2CPacket(
                condition.getSound(), SoundCategory.AMBIENT, this.getPos(), 1.0F, 1.0F));
            return;
        }

        ConditionTrigger trigger = this.triggers.get(id);
        int playDelay = condition.getPlayDelay();
        int lastPlay = trigger.lastPlay;

        if (!condition.shouldLoopPlay() && trigger.isTriggered) {
            return;
        }

        trigger.isTriggered = true;
        if (this.tick - playDelay >= lastPlay) {
            trigger.lastPlay = this.tick;
            this.networkHandler.sendPacket(new PlaySoundIdS2CPacket(
                condition.getSound(), SoundCategory.AMBIENT, this.getPos(), 1.0F, 1.0F));
        }
    }

    private void sendStopSound(Condition condition) {
        Identifier id = condition.getId();
        if (!this.triggers.containsKey(id)) {
            return;
        }

        ConditionTrigger trigger = this.triggers.get(id);
        Identifier sound = condition.getSound();
        if (trigger.isTriggered && (trigger.lastPlay + condition.getPlayDelay() > this.tick)) {
            this.networkHandler.sendPacket(new StopSoundS2CPacket(sound, SoundCategory.AMBIENT));
        }
        trigger.isTriggered = false;
    }
}
