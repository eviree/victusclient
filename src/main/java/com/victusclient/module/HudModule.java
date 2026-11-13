package com.victusclient.module;

import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class HudModule extends Module {
    public int x, y, w = 60, h = 16;
    protected final Setting.Bool background = add(new Setting.Bool("Background", true));
    protected final Setting.Bool shadow = add(new Setting.Bool("Text Shadow", true));
    protected final Setting.Mode color = add(new Setting.Mode("Color", 0, "White", "Crimson", "Red", "Green", "Cyan", "Yellow"));
    private static final int[] COLORS = {0xFFFFFFFF, 0xFFFF4D6D, 0xFFFF5555, 0xFF55FF55, 0xFF55FFFF, 0xFFFFFF55};

    protected HudModule(String name, int x, int y) {
        super(name, Category.HUD);
        this.x = x; this.y = y;
    }

    protected int textColor() { return COLORS[color.index]; }

    protected abstract void draw(DrawContext ctx, MinecraftClient mc);

    @Override
    public void onRender(DrawContext ctx, MinecraftClient mc) { draw(ctx, mc); }

    /** Simple one-line text box. Also updates w/h so the HUD editor knows the size. */
    protected void box(DrawContext ctx, MinecraftClient mc, String text) {
        w = mc.textRenderer.getWidth(text) + 10;
        h = 16;
        if (background.value) ctx.fill(x, y, x + w, y + h, 0x90000000);
        ctx.drawText(mc.textRenderer, text, x + 5, y + 4, textColor(), shadow.value);
    }
}
