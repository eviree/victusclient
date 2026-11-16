package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;

/**
 * Caps particle density for smoother FPS in particle-heavy situations
 * (fireworks, large farms, nether portals, etc.).
 */
public class ParticleLimiterModule extends Module {
    public static ParticleLimiterModule INSTANCE;

    public final Setting.Slider maxParticles = add(new Setting.Slider("Max particles", 200, 50, 1000, 25));
    public final Setting.Bool reduceWeather = add(new Setting.Bool("Reduce weather particles", true));
    public final Setting.Bool reduceExplosions = add(new Setting.Bool("Reduce explosion particles", true));

    public ParticleLimiterModule() {
        super("Particle Limiter", Category.UTILITY);
        INSTANCE = this;
    }

    public static int maxAllowed() {
        ParticleLimiterModule m = INSTANCE;
        if (m == null || !m.enabled) return Integer.MAX_VALUE;
        return (int) m.maxParticles.value;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        // actual limiting is done where particles are added if a mixin is present;
        // this module still exposes the setting and can be toggled from the menu
    }
}
