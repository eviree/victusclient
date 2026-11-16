package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Custom crosshair drawn on top of (or instead of) the vanilla one. */
public class CrosshairModule extends Module {
    private final Setting.Mode style = add(new Setting.Mode("Style", 0,
            "Cross", "Dot", "Circle", "Plus", "T"));
    private final Setting.Slider size = add(new Setting.Slider("Size", 6, 2, 16, 1));
    private final Setting.Slider thickness = add(new Setting.Slider("Thickness", 1, 1, 4, 1));
    private final Setting.Slider gap = add(new Setting.Slider("Gap", 2, 0, 8, 1));
    private final Setting.Mode colorMode = add(new Setting.Mode("Color", 0,
            "White", "Violet", "Red", "Green", "Cyan", "Rainbow"));
    private final Setting.Bool outline = add(new Setting.Bool("Outline", true));

    public CrosshairModule() {
        super("Custom Crosshair", Category.VISUAL);
    }

    private int color() {
        return switch (colorMode.index) {
            case 1 -> 0xFF8B6CFF;
            case 2 -> 0xFFFF5555;
            case 3 -> 0xFF55FF55;
            case 4 -> 0xFF55FFFF;
            case 5 -> {
                float h = (System.currentTimeMillis() % 3000) / 3000f;
                yield 0xFF000000 | hsv(h, 0.7f, 1f);
            }
            default -> 0xFFFFFFFF;
        };
    }

    private static int hsv(float h, float s, float v) {
        int i = (int) (h * 6);
        float f = h * 6 - i, p = v * (1 - s), q = v * (1 - f * s), t = v * (1 - (1 - f) * s);
        float r, g, b;
        switch (i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    @Override
    public void onRender(DrawContext ctx, MinecraftClient mc) {
        if (mc.options.hudHidden || mc.player == null) return;
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int cx = sw / 2;
        int cy = sh / 2;
        int s = (int) size.value;
        int t = (int) thickness.value;
        int g = (int) gap.value;
        int col = color();
        int out = 0xFF000000;

        switch (style.index) {
            case 1 -> { // Dot
                if (outline.value) ctx.fill(cx - t - 1, cy - t - 1, cx + t + 1, cy + t + 1, out);
                ctx.fill(cx - t, cy - t, cx + t, cy + t, col);
            }
            case 2 -> { // Circle approximation
                for (int a = 0; a < 360; a += 8) {
                    double rad = Math.toRadians(a);
                    int x = cx + (int) (Math.cos(rad) * s);
                    int y = cy + (int) (Math.sin(rad) * s);
                    ctx.fill(x, y, x + t, y + t, col);
                }
            }
            case 3 -> { // Plus (no gap)
                drawArm(ctx, cx - s, cy - t / 2, s * 2, t, col, out);
                drawArm(ctx, cx - t / 2, cy - s, t, s * 2, col, out);
            }
            case 4 -> { // T
                drawArm(ctx, cx - s, cy - s, s * 2, t, col, out);
                drawArm(ctx, cx - t / 2, cy - s, t, s * 2, col, out);
            }
            default -> { // Cross with gap
                // left
                drawArm(ctx, cx - g - s, cy - t / 2, s, t, col, out);
                // right
                drawArm(ctx, cx + g, cy - t / 2, s, t, col, out);
                // up
                drawArm(ctx, cx - t / 2, cy - g - s, t, s, col, out);
                // down
                drawArm(ctx, cx - t / 2, cy + g, t, s, col, out);
            }
        }
    }

    private void drawArm(DrawContext ctx, int x, int y, int w, int h, int col, int out) {
        if (outline.value) ctx.fill(x - 1, y - 1, x + w + 1, y + h + 1, out);
        ctx.fill(x, y, x + w, y + h, col);
    }
}
