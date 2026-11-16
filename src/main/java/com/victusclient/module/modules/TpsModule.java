package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

/**
 * Approximate server TPS from client tick timing / world time deltas.
 */
public class TpsModule extends HudModule {
    private long lastWorldTime = -1;
    private long lastNano = -1;
    private double tps = 20.0;
    private final double[] samples = new double[20];
    private int sampleIdx;

    public TpsModule() {
        super("TPS", 6, 26);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.world == null) return;
        long wt = mc.world.getTime();
        long now = System.nanoTime();
        if (lastWorldTime >= 0 && lastNano > 0 && wt > lastWorldTime) {
            long dWorld = wt - lastWorldTime;
            double dSec = (now - lastNano) / 1_000_000_000.0;
            if (dSec > 0.05 && dSec < 5.0) {
                double instant = MathHelper.clamp(dWorld / dSec, 0.0, 20.0);
                samples[sampleIdx++ % samples.length] = instant;
                double sum = 0;
                int n = 0;
                for (double s : samples) {
                    if (s > 0) { sum += s; n++; }
                }
                if (n > 0) tps = sum / n;
            }
        }
        lastWorldTime = wt;
        lastNano = now;
    }

    @Override
    protected void draw(DrawContext ctx, MinecraftClient mc) {
        String s = String.format("TPS: %.1f", tps);
        box(ctx, mc, s);
    }
}
