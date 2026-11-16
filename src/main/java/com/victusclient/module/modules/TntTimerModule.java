package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.MathHelper;

/** Shows a countdown above primed TNT. */
public class TntTimerModule extends Module {
    private final Setting.Bool shadow = add(new Setting.Bool("Text shadow", true));
    private final Setting.Slider scale = add(new Setting.Slider("Scale", 1.0, 0.6, 2.0, 0.1));

    public TntTimerModule() {
        super("TNT Timer", Category.WORLD);
    }

    @Override
    public void onRender(DrawContext ctx, MinecraftClient mc) {
        if (mc.world == null || mc.player == null || mc.gameRenderer == null) return;
        // World-space labels need entity projection; approximate with nearby TNT using simple screen-space estimate
        // Full accurate projection needs WorldRenderEvents; keep a simple nearby list in HUD corner as fallback.
        int y = 40;
        int count = 0;
        for (var e : mc.world.getEntities()) {
            if (!(e instanceof TntEntity tnt)) continue;
            float fuse = tnt.getFuse() / 20f;
            double dist = mc.player.squaredDistanceTo(tnt);
            if (dist > 48 * 48) continue;
            String s = String.format("TNT %.1fs (%.0fm)", fuse, Math.sqrt(dist));
            int color = fuse < 1.5f ? 0xFFFF5555 : (fuse < 3f ? 0xFFFFAA00 : 0xFFFFFFFF);
            ctx.drawText(mc.textRenderer, s, 6, y, color, shadow.value);
            y += 10;
            if (++count >= 8) break;
        }
    }
}
