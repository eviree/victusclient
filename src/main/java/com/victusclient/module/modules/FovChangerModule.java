package com.victusclient.module.modules;

import com.victusclient.ZoomState;
import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;

/** FOV beyond the vanilla 30-110 range. */
public class FovChangerModule extends Module {
    private final Setting.Slider fov = add(new Setting.Slider("FOV", 100, 30, 140, 1));

    public FovChangerModule() { super("FOV Changer", Category.VISUAL); }

    @Override public void onTick(MinecraftClient mc) {
        double base = Math.max(1, mc.options.getFov().getValue());
        ZoomState.fovMul = fov.value / base;
    }

    @Override public void onDisable() { ZoomState.fovMul = 1.0; }
}
