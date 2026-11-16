package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

/** Keys (set them in Controls): toggle start/pause, and reset. */
public class StopwatchModule extends HudModule {
    public static KeyBinding TOGGLE, RESET;
    private long accumulated, startedAt;
    private boolean running;

    public StopwatchModule() { super("Stopwatch", 6, 520); }

    @Override public void onTick(MinecraftClient mc) {
        while (TOGGLE != null && TOGGLE.wasPressed()) {
            if (running) { accumulated += System.currentTimeMillis() - startedAt; running = false; }
            else { startedAt = System.currentTimeMillis(); running = true; }
        }
        while (RESET != null && RESET.wasPressed()) { accumulated = 0; running = false; }
    }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        long t = accumulated + (running ? System.currentTimeMillis() - startedAt : 0);
        long s = t / 1000;
        box(ctx, mc, String.format("%02d:%02d.%d %s", s / 60, s % 60, (t / 100) % 10, running ? "" : "(paused)"));
    }
}
