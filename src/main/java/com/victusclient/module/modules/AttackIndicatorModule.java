package com.victusclient.module.modules;

import com.victusclient.gui.UI;
import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Attack cooldown bar under the crosshair. */
public class AttackIndicatorModule extends Module {
    private final Setting.Bool always = add(new Setting.Bool("Always Show", false));

    public AttackIndicatorModule() { super("Attack Indicator", Category.PVP); }

    @Override public void onRender(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;
        float p = mc.player.getAttackCooldownProgress(0f);
        if (p >= 1f && !always.value) return;
        int cx = mc.getWindow().getScaledWidth() / 2, cy = mc.getWindow().getScaledHeight() / 2;
        int bw = 26, x = cx - bw / 2, y = cy + 12;
        ctx.fill(x - 1, y - 1, x + bw + 1, y + 3, 0xA0000000);
        UI.hgrad(ctx, x, y, (int) (bw * Math.min(1f, p)), 2, UI.ACCENT_DIM, p >= 1f ? UI.GREEN : UI.ACCENT_LIGHT);
    }
}
