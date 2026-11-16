package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;

/**
 * One-toggle performance profile. Applies sensible client options for
 * higher and more stable FPS without permanently locking the user out
 * of their preferred graphics settings (restores on disable when possible).
 */
public class FpsBoostModule extends Module {
    private final Setting.Bool lowerClouds = add(new Setting.Bool("Lower cloud quality", true));
    private final Setting.Bool reduceParticles = add(new Setting.Bool("Reduce particles", true));
    private final Setting.Bool fastGraphics = add(new Setting.Bool("Prefer Fast graphics", false));
    private final Setting.Bool entityShadows = add(new Setting.Bool("Disable entity shadows", true));
    private final Setting.Slider viewDistanceCap = add(new Setting.Slider("View distance cap", 12, 4, 32, 1));

    private GraphicsMode prevGraphics;
    private ParticlesMode prevParticles;
    private CloudRenderMode prevClouds;
    private boolean prevEntityShadows;
    private int prevViewDist = -1;
    private boolean applied;

    public FpsBoostModule() {
        super("FPS Boost", Category.UTILITY);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options == null) return;

        prevGraphics = mc.options.getGraphicsMode().getValue();
        prevParticles = mc.options.getParticles().getValue();
        prevClouds = mc.options.getCloudRenderMode().getValue();
        prevEntityShadows = mc.options.getEntityShadows().getValue();
        prevViewDist = mc.options.getViewDistance().getValue();
        applied = true;

        if (fastGraphics.value) {
            mc.options.getGraphicsMode().setValue(GraphicsMode.FAST);
        }
        if (reduceParticles.value) {
            mc.options.getParticles().setValue(ParticlesMode.DECREASED);
        }
        if (lowerClouds.value) {
            mc.options.getCloudRenderMode().setValue(CloudRenderMode.FAST);
        }
        if (entityShadows.value) {
            mc.options.getEntityShadows().setValue(false);
        }
        int cap = (int) viewDistanceCap.value;
        if (prevViewDist > cap) {
            mc.options.getViewDistance().setValue(cap);
        }
    }

    @Override
    public void onDisable() {
        if (!applied) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options == null) return;

        if (prevGraphics != null) mc.options.getGraphicsMode().setValue(prevGraphics);
        if (prevParticles != null) mc.options.getParticles().setValue(prevParticles);
        if (prevClouds != null) mc.options.getCloudRenderMode().setValue(prevClouds);
        mc.options.getEntityShadows().setValue(prevEntityShadows);
        if (prevViewDist > 0) mc.options.getViewDistance().setValue(prevViewDist);
        applied = false;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        // keep the profile active while enabled
    }
}
