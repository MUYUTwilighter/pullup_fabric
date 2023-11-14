package cool.muyucloud.pullup.mixin;

import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.Registry;
import cool.muyucloud.pullup.util.condition.ConditionLoader;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.Objects;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Unique
    private static final Logger LOGGER = Pullup.getLogger();
    @Unique
    private static final Config CONFIG = Pullup.getConfig();

    @Inject(method = "onGameJoin", at = @At("HEAD"))
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        if (!CONFIG.getAsBool("loadServer")) {
            try {
                this.loadConditions();
            } catch (Exception e) {
                LOGGER.error("Try to load local condition set but failed.", e);
            }
        }
    }

    @Unique
    private void loadConditions() throws IOException {
        Registry.CONDITIONS.clear();
        String loadSet = CONFIG.getAsString("loadSet");
        if (Objects.equals(loadSet, "default")) {
            new ConditionLoader().load();
        } else {
            new ConditionLoader(loadSet).load();
        }
    }
}
