package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;

/** Client-side time of day override. Only you see it. */
public class TimeChangerModule extends Module {
    private final Setting.Mode mode = add(new Setting.Mode("Mode", 0,
            "Day", "Noon", "Sunset", "Night", "Midnight", "Sunrise", "Custom"));
    private final Setting.Slider custom = add(new Setting.Slider("Custom time", 6000, 0, 24000, 500));

    public TimeChangerModule() {
        super("Time Changer", Category.WORLD);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.world == null) return;
        long t = switch (mode.index) {
            case 0 -> 1000L;   // morning
            case 1 -> 6000L;   // noon
            case 2 -> 12000L;  // sunset
            case 3 -> 13000L;  // night
            case 4 -> 18000L;  // midnight
            case 5 -> 23000L;  // sunrise
            default -> (long) custom.value;
        };
        mc.world.getLevelProperties().setTimeOfDay(t);
    }
}
