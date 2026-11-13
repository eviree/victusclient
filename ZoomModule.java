package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

/** Hold C to zoom. */
public class ZoomModule extends Module {
    private final Setting.Slider zoomFov = add(new Setting.Slider("Zoom FOV", 30, 30, 70, 1));
    private boolean zooming;
    private int savedFov;

    public ZoomModule() { super("Zoom", Category.RENDER); }

    @Override public void onTick(MinecraftClient mc) {
        boolean held = mc.currentScreen == null
                && GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_C) == GLFW.GLFW_PRESS;
        if (held && !zooming) {
            savedFov = mc.options.getFov().getValue();
            zooming = true;
        }
        if (held) mc.options.getFov().setValue((int) zoomFov.value);
        else if (zooming) restore(mc);
    }

    private void restore(MinecraftClient mc) {
        mc.options.getFov().setValue(savedFov);
        zooming = false;
    }

    @Override public void onDisable() {
        if (zooming) restore(MinecraftClient.getInstance());
    }
}
