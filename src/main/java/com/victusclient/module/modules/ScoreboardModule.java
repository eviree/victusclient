package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;

/**
 * Scoreboard controls. Full restyle needs scoreboard render mixins;
 * this provides hide / scale-style toggles that other code can read.
 */
public class ScoreboardModule extends Module {
    public static ScoreboardModule INSTANCE;

    public final Setting.Bool hide = add(new Setting.Bool("Hide scoreboard", false));
    public final Setting.Slider scale = add(new Setting.Slider("Scale %", 100, 50, 150, 5));
    public final Setting.Bool shadow = add(new Setting.Bool("Text shadow", true));

    public ScoreboardModule() {
        super("Scoreboard", Category.UTILITY);
        INSTANCE = this;
    }

    public static boolean shouldHide() {
        return INSTANCE != null && INSTANCE.enabled && INSTANCE.hide.value;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        // flag module for mixins / future render hooks
    }
}
