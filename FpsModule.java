package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class FpsModule extends HudModule {
    public FpsModule() { super("FPS", 6, 6); }
    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        box(ctx, mc, "FPS: " + mc.getCurrentFps());
    }
}
