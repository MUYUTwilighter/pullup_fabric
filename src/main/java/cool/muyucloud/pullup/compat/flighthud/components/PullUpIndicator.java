package cool.muyucloud.pullup.compat.flighthud.components;

import com.plr.flighthud.api.HudComponent;
import com.plr.flighthud.common.Dimensions;
import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.access.ClientPlayerEntityAccess;
import cool.muyucloud.pullup.util.condition.Condition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class PullUpIndicator extends HudComponent {
    private final Dimensions dims;

    public PullUpIndicator(Dimensions dims) {
        this.dims = dims;
    }

    @Override
    public void render(DrawContext drawContext, float v, MinecraftClient mc) {
        if (mc.player == null) return;
        final ClientPlayerEntityAccess access = (ClientPlayerEntityAccess) mc.player;
        final List<Condition.ColoredText> texts = access.getHudTexts();
        if (texts.isEmpty()) return;
        final float x = dims.wScreen * Pullup.getConfig().getAsFloat("hudTextDisplayX");
        final float y = dims.hScreen * Pullup.getConfig().getAsFloat("hudTextDisplayY");
        for (int i = 0; i < texts.size(); i++) {
            final Condition.ColoredText text = texts.get(i);
            drawContext.drawText(mc.textRenderer, text.text(), (int) x, (int) (y + 10 * i), text.color(), false);
        }
    }
}
