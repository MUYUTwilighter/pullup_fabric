package cool.muyucloud.pullup.client;

import cool.muyucloud.pullup.compat.CompatHandler;
import net.fabricmc.api.ClientModInitializer;

public class PullupClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CompatHandler.init();
    }
}
