package com.victusclient.module.modules;

import com.victusclient.gui.UI;
import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Requires pressing the drop key twice so you never throw items by accident. */
public class DropPreventionModule extends Module {
    private static DropPreventionModule INSTANCE;
    private final Setting.Slider window = add(new Setting.Slider("Confirm Window (ms)", 600, 300, 1500, 50));
    private long first;

    public DropPreventionModule() { super("Drop Prevention", Category.UTILITY); INSTANCE = this; }

    /** Called at the START of every client tick, before vanilla reads the drop key. */
    public static void startTick(MinecraftClient mc) {
        DropPreventionModule m = INSTANCE;
        if (m == null || !m.enabled || mc.player == null || mc.currentScreen != null) return;
        while (mc.options.dropKey.wasPressed()) {
            long now = System.currentTimeMillis();
            if (m.first != 0 && now - m.first <= m.window.value) {
                mc.player.dropSelectedItem(false);
                m.first = 0;
            } else {
                m.first = now;
            }
        }
    }

    @Override public void onRender(DrawContext ctx, MinecraftClient mc) {
        if (first == 0 || System.currentTimeMillis() - first > window.value) return;
        String t = "Press again to drop";
        int x = (mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth(t)) / 2;
        ctx.drawText(mc.textRenderer, t, x, mc.getWindow().getScaledHeight() - 66, UI.ACCENT_LIGHT, true);
    }
}
