package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.util.List;

public class MemoryModule extends HudModule {
    public MemoryModule() { super("System Resources", 6, 250); }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        Runtime r = Runtime.getRuntime();
        long used = (r.totalMemory() - r.freeMemory()) / 1048576L;
        long max = r.maxMemory() / 1048576L;
        lines(ctx, mc, List.of("RAM: " + used + " / " + max + " MB (" + (used * 100 / Math.max(1, max)) + "%)",
                "Cores: " + r.availableProcessors()));
    }
}
