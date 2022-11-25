package cool.muyucloud.pullup.util.network;

import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.Config;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.Date;

public class PullupNetworkC2S {
    private static final Config CONFIG = Pullup.getConfig();
    public static final Identifier GRAB_CONDITIONS = new Identifier("pullup:grab_conditions");
    private static long LAST_SEND = new Date().getTime();

    public static void registerReceive() {
        ServerPlayNetworking.registerGlobalReceiver(GRAB_CONDITIONS,
            (server, player, handler, buf, responseSender) -> receiveGrab(server, responseSender));
    }

    private static void receiveGrab(MinecraftServer server, PacketSender sender) {
        server.execute(() -> {
            long tmp = new Date().getTime();
            if (LAST_SEND + CONFIG.getAsInt("sendDelay") >= tmp || CONFIG.getAsBool("sendServer")) {
                sender.sendPacket(PullupNetworkS2C.REFUSE, PacketByteBufs.empty());
                return;
            }
            LAST_SEND = tmp;
            sender.sendPacket(PullupNetworkS2C.CLEAR_CONDITIONS, PacketByteBufs.empty());
            sender.sendPacket(PullupNetworkS2C.LOAD_CONDITIONS, PullupNetworkS2C.assembleLoadBuf());
        });
    }

    public static void sendGrab() {
        ClientPlayNetworking.send(GRAB_CONDITIONS, PacketByteBufs.empty());
    }
}
