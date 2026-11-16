package com.victusclient;

import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWScrollCallback;

/** Smooth zoom: exponential easing in log-space, true tan-based magnification, scroll to change level. */
public final class ZoomState {
    private ZoomState() {}

    public static volatile boolean zooming;
    public static volatile boolean scrollEnabled = true;
    public static volatile double smooth = 14.0;       // higher = faster
    public static Setting.Slider levelSetting;          // zoom level (magnification), set by the module
    public static volatile double fovMul = 1.0;         // set by the FOV Changer module

    private static double logMag = 0.0;                 // ln(current magnification)
    private static long lastNs = 0L;

    public static double mag() { return Math.exp(logMag); }

    /** Called by the GameRenderer#getFov mixin (every frame). */
    public static double apply(double fov) {
        fov *= fovMul;
        long now = System.nanoTime();
        double dt = lastNs == 0L ? 0.016 : Math.min(0.1, (now - lastNs) / 1.0e9);
        lastNs = now;

        double level = levelSetting != null ? Math.max(1.0, levelSetting.value) : 4.0;
        double target = zooming ? Math.log(level) : 0.0;
        logMag += (target - logMag) * (1.0 - Math.exp(-dt * smooth));
        if (Math.abs(target - logMag) < 0.0005) logMag = target;

        double m = Math.exp(logMag);
        if (m <= 1.0005) return fov;
        double half = Math.toRadians(fov) / 2.0;
        return Math.toDegrees(2.0 * Math.atan(Math.tan(half) / m));
    }

    // ---- scroll wheel changes zoom level while zooming (no mixin: chains the GLFW callback) ----
    private static boolean installed;
    private static GLFWScrollCallback ours; // keep a reference so it isn't garbage collected

    public static void installScroll(long window) {
        if (installed) return;
        installed = true;
        final GLFWScrollCallback prev = GLFW.glfwSetScrollCallback(window, null);
        ours = GLFWScrollCallback.create((w, xOff, yOff) -> {
            if (zooming && scrollEnabled && levelSetting != null
                    && MinecraftClient.getInstance().currentScreen == null && yOff != 0) {
                double v = levelSetting.value * (yOff > 0 ? 1.15 : 1.0 / 1.15);
                v = Math.max(levelSetting.min, Math.min(levelSetting.max, v));
                levelSetting.value = Math.round(v * 10.0) / 10.0;
                return; // consume: don't change hotbar slot
            }
            if (prev != null) prev.invoke(w, xOff, yOff);
        });
        GLFW.glfwSetScrollCallback(window, ours);
    }
}
