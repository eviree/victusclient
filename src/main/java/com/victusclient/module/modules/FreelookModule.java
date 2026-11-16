package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import org.lwjgl.glfw.GLFW;

/**
 * Perspective / freelook style helper.
 * Holds a key to temporarily switch to third person and remember yaw/pitch offset.
 * Full free camera (detach camera from player body) needs Camera mixin.
 */
public class FreelookModule extends Module {
    public static FreelookModule INSTANCE;

    public final Setting.Mode keyMode = add(new Setting.Mode("Activation", 0, "Hold Left Alt", "Toggle"));
    public final Setting.Bool returnOnRelease = add(new Setting.Bool("Return perspective", true));

    private Perspective prev;
    private boolean active;
    private boolean toggled;

    public FreelookModule() {
        super("Perspective", Category.VISUAL);
        INSTANCE = this;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.getWindow() == null) return;
        long win = mc.getWindow().getHandle();
        boolean held = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS;

        boolean want;
        if (keyMode.index == 0) {
            want = held;
        } else {
            // toggle on rising edge would need edge detect; simple hold of alt still works as fallback
            if (held && !active) toggled = !toggled;
            want = toggled;
        }

        if (want && !active) {
            prev = mc.options.getPerspective();
            mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
            active = true;
        } else if (!want && active) {
            if (returnOnRelease.value && prev != null) {
                mc.options.setPerspective(prev);
            }
            active = false;
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (active && returnOnRelease.value && prev != null && mc.options != null) {
            mc.options.setPerspective(prev);
        }
        active = false;
        toggled = false;
    }
}
