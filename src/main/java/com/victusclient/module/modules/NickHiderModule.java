package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;

/** Hides or replaces your own name in local display contexts. */
public class NickHiderModule extends Module {
    public static NickHiderModule INSTANCE;

    public final Setting.Mode mode = add(new Setting.Mode("Mode", 0, "Hidden", "Custom"));
    public final Setting.Mode custom = add(new Setting.Mode("Custom name", 0,
            "Player", "You", "Me", "Hidden"));

    public NickHiderModule() {
        super("Nick Hider", Category.UTILITY);
        INSTANCE = this;
    }

    public static String disguise(String original) {
        NickHiderModule m = INSTANCE;
        if (m == null || !m.enabled) return original;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return original;
        String self = mc.player.getName().getString();
        if (original == null || !original.equalsIgnoreCase(self)) return original;
        if (m.mode.index == 0) return "******";
        return switch (m.custom.index) {
            case 1 -> "You";
            case 2 -> "Me";
            case 3 -> "******";
            default -> "Player";
        };
    }
}
