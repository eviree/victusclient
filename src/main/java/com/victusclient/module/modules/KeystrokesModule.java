package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

public class KeystrokesModule extends HudModule {
    public KeystrokesModule() { super("Keystrokes", 6, 70); w = 64; h = 42; }

    private void key(DrawContext ctx, MinecraftClient mc, int kx, int ky, int kw, String label, KeyBinding kb) {
        boolean down = kb.isPressed();
        if (background.value || down) ctx.fill(kx, ky, kx + kw, ky + 20, down ? 0xA0FFFFFF : 0x90000000);
        int tw = mc.textRenderer.getWidth(label);
        ctx.drawText(mc.textRenderer, label, kx + (kw - tw) / 2, ky + 6, down ? 0xFF000000 : textColor(), shadow.value && !down);
    }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        w = 64; h = 42;
        key(ctx, mc, x + 22, y, 20, "W", mc.options.forwardKey);
        key(ctx, mc, x, y + 22, 20, "A", mc.options.leftKey);
        key(ctx, mc, x + 22, y + 22, 20, "S", mc.options.backKey);
        key(ctx, mc, x + 44, y + 22, 20, "D", mc.options.rightKey);
    }
}
