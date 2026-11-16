package com.victusclient.module.modules;

import com.victusclient.ZoomState;
import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

/** Smooth zoom. Hold the Zoom key (default C, rebindable in Controls). Scroll wheel changes the level. */
public class ZoomModule extends Module {
    public static KeyBinding KEY; // registered in VictusClient

    private final Setting.Slider level = add(new Setting.Slider("Zoom Level", 4.0, 1.5, 30.0, 0.1));
    private final Setting.Slider smoothness = add(new Setting.Slider("Smoothness", 14, 4, 30, 1));
    private final Setting.Bool scroll = add(new Setting.Bool("Scroll To Zoom", true));
    private final Setting.Bool lowerSens = add(new Setting.Bool("Reduce Sensitivity", true));

    private static boolean sensSaved;
    private static double savedSens;

    public ZoomModule() { super("Zoom", Category.VISUAL); }

    @Override public void onTick(MinecraftClient mc) {
        ZoomState.installScroll(mc.getWindow().getHandle());
        ZoomState.levelSetting = level;
        ZoomState.smooth = smoothness.value;
        ZoomState.scrollEnabled = scroll.value;

        boolean held = mc.currentScreen == null && KEY != null && KEY.isPressed();
        ZoomState.zooming = held;

        if (lowerSens.value && held) {
            if (!sensSaved) { savedSens = mc.options.getMouseSensitivity().getValue(); sensSaved = true; }
            mc.options.getMouseSensitivity().setValue(Math.max(0.0, Math.min(1.0, savedSens / Math.max(1.0, ZoomState.mag()))));
        } else if (sensSaved) {
            restoreSensitivity();
        }
    }

    public static void restoreSensitivity() {
        if (!sensSaved) return;
        MinecraftClient.getInstance().options.getMouseSensitivity().setValue(savedSens);
        sensSaved = false;
    }

    @Override public void onDisable() {
        ZoomState.zooming = false;
        restoreSensitivity();
    }
}
