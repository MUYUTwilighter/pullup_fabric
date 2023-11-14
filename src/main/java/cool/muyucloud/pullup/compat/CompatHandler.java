package cool.muyucloud.pullup.compat;

import cool.muyucloud.pullup.compat.flighthud.FlightHudCompat;

public class CompatHandler {

    public static final boolean flightHudLoaded;

    static {
        boolean loaded;
        try {
            Class.forName("com.plr.flighthud.api.HudRegistry");
            loaded = true;
        } catch (ClassNotFoundException e) {
            loaded = false;
        }
        flightHudLoaded = loaded;
    }

    public static void init() {
        if (flightHudLoaded) FlightHudCompat.init();
    }
}
