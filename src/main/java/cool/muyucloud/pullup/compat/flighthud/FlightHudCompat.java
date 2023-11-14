package cool.muyucloud.pullup.compat.flighthud;

import com.plr.flighthud.api.HudRegistry;
import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.compat.flighthud.components.PullUpIndicator;

public class FlightHudCompat {
    public static void init() {
        if (!HudRegistry.addComponent(($, dims) -> new PullUpIndicator(dims)))
            Pullup.getLogger().warn("Failed to add component to flight hud for pullup.");
    }
}
