package com.victusclient.module.modules;

import com.victusclient.gui.UI;
import com.victusclient.module.Category;
import com.victusclient.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Red edge vignette when you take damage. */
public class DamageIndicatorModule extends Module {
    public DamageIndicatorModule() { super("Damage Indicator", Category.PVP); }

    @Override public void onRender(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null || mc.player.hurtTime <= 0) return;
        float a = mc.player.hurtTime / (float) Math.max(1, mc.player.maxHurtTime);
        int w = mc.getWindow().getScaledWidth(), h = mc.getWindow().getScaledHeight();
        int red = UI.alpha(0xFFFF2244, 0.55f * a), none = 0x00FF2244;
        int t = Math.max(24, h / 6), s = Math.max(24, w / 8);
        UI.vgrad(ctx, 0, 0, w, t, red, none);
        UI.vgrad(ctx, 0, h - t, w, t, none, red);
        UI.hgrad(ctx, 0, 0, s, h, red, none);
        UI.hgrad(ctx, w - s, 0, s, h, none, red);
    }
}
