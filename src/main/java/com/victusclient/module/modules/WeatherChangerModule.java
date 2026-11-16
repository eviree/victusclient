package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;

/** Client-side weather override (only you see it). */
public class WeatherChangerModule extends Module {
    private final Setting.Mode weather = add(new Setting.Mode("Weather", 0, "Clear", "Rain", "Thunder"));

    public WeatherChangerModule() { super("Weather Changer", Category.WORLD); }

    @Override public void onTick(MinecraftClient mc) {
        if (mc.world == null) return;
        float rain = weather.index == 0 ? 0f : 1f;
        float thunder = weather.index == 2 ? 1f : 0f;
        mc.world.setRainGradient(rain);
        mc.world.setThunderGradient(thunder);
    }
}
