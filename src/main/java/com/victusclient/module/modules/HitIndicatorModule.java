package com.victusclient.module.modules;

import com.victusclient.gui.UI;
import com.victusclient.module.Category;
import com.victusclient.module.Module;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;

/** Small marker around the crosshair when you hit something. */
public class HitIndicatorModule extends Module {
    private long lastHit;

    public HitIndicatorModule() {
        super("Hit Indicator", Category.PVP);
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (enabled && player == MinecraftClient.getInstance().player && entity instanceof LivingEntity)
                lastHit = System.currentTimeMillis();
            return ActionResult.PASS;
        });
    }

    @Override public void onRender(DrawContext ctx, MinecraftClient mc) {
        long age = System.currentTimeMillis() - lastHit;
        if (age > 280) return;
        float a = 1f - age / 280f;
        int cx = mc.getWindow().getScaledWidth() / 2, cy = mc.getWindow().getScaledHeight() / 2;
        int off = 5 + (int) ((1f - a) * 4), col = UI.alpha(0xFFFFFFFF, a);
        for (int i = 0; i < 4; i++) {
            ctx.fill(cx - off - i, cy - off - i, cx - off - i + 2, cy - off - i + 2, col);
            ctx.fill(cx + off + i - 1, cy - off - i, cx + off + i + 1, cy - off - i + 2, col);
            ctx.fill(cx - off - i, cy + off + i - 1, cx - off - i + 2, cy + off + i + 1, col);
            ctx.fill(cx + off + i - 1, cy + off + i - 1, cx + off + i + 1, cy + off + i + 1, col);
        }
    }
}
