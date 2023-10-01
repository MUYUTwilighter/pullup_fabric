package cool.muyucloud.pullup.mixin;

import com.mojang.authlib.GameProfile;
import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.access.ClientPlayerEntityAccess;
import cool.muyucloud.pullup.util.condition.Condition;
import cool.muyucloud.pullup.util.condition.ConditionTrigger;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.Registry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends PlayerEntity implements ClientPlayerEntityAccess {
    @Shadow
    @Override
    public abstract float getYaw(float tickDelta);

    @Shadow
    private float lastYaw;
    @Shadow
    private float lastPitch;

    @Shadow
    @Override
    public abstract void tick();

    @Shadow
    @Final
    protected MinecraftClient client;
    @Unique
    private static final Config CONFIG = Pullup.getConfig();
    @Unique
    private int ticks = 0;
    @Unique
    private boolean isNewTick = false;
    @Unique
    private long flightStart = new Date().getTime();
    @Unique
    private final HashMap<Identifier, ConditionTrigger> conditionTriggers = new HashMap<>();
    @Unique
    private final HashSet<Identifier> triggersToRemove = new HashSet<>();

    public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        this.updateTick();
        if (!this.isNewTick) {
            return;
        }

        this.checkConditions();
        this.playSounds();
        this.clearTriggers();
    }

    @Unique
    private void checkConditions() {
        for (Condition condition : Registry.CONDITIONS.getAll()) {
            if (this.ticks % condition.getCheckDelay() != 0) {
                continue;
            }

            this.registerTrigger(condition.getId());
            ConditionTrigger trigger = this.conditionTriggers.get(condition.getId());

            if (!condition.verifyExpressions(((ClientPlayerEntity) (Object) this), this.getWorld())) {
                trigger.isTriggered = false;
                trigger.lastPlay = -1;
                continue;
            }

            trigger.isTriggered = true;
        }
    }

    @Unique
    private void registerTrigger(Identifier id) {
        if (this.conditionTriggers.containsKey(id)) {
            return;
        }

        ConditionTrigger trigger = new ConditionTrigger();
        trigger.lastPlay = -1;
        this.conditionTriggers.put(id, trigger);
    }

    @Unique
    private void playSounds() {
        if (this.client.world == null) {
            return;
        }

        for (Identifier id : this.conditionTriggers.keySet()) {
            Condition condition = Registry.CONDITIONS.get(id);
            if (condition == null) {
                this.triggersToRemove.add(id);
                continue;
            }

            ConditionTrigger trigger = this.conditionTriggers.get(id);
            if (!trigger.isTriggered) {
                continue;
            }

            if (!condition.shouldLoopPlay()) {
                if (trigger.lastPlay == -1) {
                    this.client.world.playSound(this.getX(), this.getY(), this.getZ(),
                        SoundEvent.of(condition.getSound(), 0),
                        SoundCategory.MUSIC, 1.0F, 1.0F, false);
                    trigger.lastPlay = this.ticks;
                }
                continue;
            }

            if (condition.getPlayDelay() < (this.ticks - trigger.lastPlay)) {
                trigger.lastPlay = this.ticks;
                this.client.world.playSound(this.getX(), this.getY(), this.getZ(),
                    SoundEvent.of(condition.getSound(), 0),
                    SoundCategory.MUSIC, 1.0F, 1.0F, false);
            }
        }
    }

    @Unique
    private void clearTriggers() {
        for (Identifier id : this.triggersToRemove) {
            this.conditionTriggers.remove(id);
        }
        this.triggersToRemove.clear();
    }

    @Unique
    private void updateTick() {
        if (!this.isFallFlying()) {
            this.isNewTick = false;
            this.ticks = 0;
            this.flightStart = new Date().getTime();
            return;
        }

        long tmpTime = new Date().getTime();
        int tmpTick = (int) ((tmpTime - this.flightStart) / 50);
        this.isNewTick = tmpTick != this.ticks;
        this.ticks = tmpTick;
    }

    @Unique
    @Override
    public double getDistanceHorizontal() {
        int maxDistance = CONFIG.getAsInt("maxDistance");
        Vec3d cameraPos = this.getCameraPosVec(0);
        Vec3d rotate = this.getRotationVector(0, this.getYaw());
        Vec3d endPos = cameraPos.add(rotate.x * maxDistance, rotate.y * maxDistance, rotate.z * maxDistance);
        Vec3d target = this.getWorld().raycast(new RaycastContext(cameraPos, endPos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, this)).getPos();
        return cameraPos.distanceTo(target);
    }

    @Unique
    @Override
    public double getPitchedDistanceAhead(float pitch) {
        int maxDistance = CONFIG.getAsInt("maxDistance");
        Vec3d cameraPos = this.getCameraPosVec(0);
        Vec3d rotate = this.getRotationVector(this.getPitch() + pitch, this.getYaw());
        Vec3d endPos = cameraPos.add(rotate.x * maxDistance, rotate.y * maxDistance, rotate.z * maxDistance);
        Vec3d target = this.getWorld().raycast(new RaycastContext(cameraPos, endPos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, this)).getPos();
        return cameraPos.distanceTo(target);
    }

    @Unique
    @Override
    public double getRelativeHeight() {
        int maxDistance = CONFIG.getAsInt("maxDistance");
        Vec3d cameraPos = this.getCameraPosVec(0);
        Vec3d endPos = cameraPos.add(0, -maxDistance, 0);
        Vec3d target = this.getWorld().raycast(new RaycastContext(cameraPos, endPos, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, this)).getPos();
        return cameraPos.distanceTo(target);
    }

    @Unique
    @Override
    public double getDeltaYaw() {
        return this.getYaw() - this.lastYaw;
    }

    @Unique
    @Override
    public double getDeltaPitch() {
        return this.getPitch() - this.lastPitch;
    }

    @Unique
    @Override
    public double getFlightTicks() {
        return this.ticks;
    }
}
