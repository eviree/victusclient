package com.victusclient.module.modules;

import com.victusclient.gui.UI;
import com.victusclient.module.Category;
import com.victusclient.module.HudModule;
import com.victusclient.setting.Setting;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;

/**
 * VICTUS MOMENTUM - the signature Victus mod.
 * Every hit builds momentum; getting hit breaks it. The bar glows, hits 75% = OVERDRIVE,
 * and every 5th hit pops a milestone. Pure visual feedback, no gameplay effect.
 */
public class MomentumModule extends HudModule {
    private final Setting.Slider decay = add(new Setting.Slider("Decay Speed", 8, 2, 25, 1));
    private final Setting.Bool popups = add(new Setting.Bool("Milestone Popups", true));

    private double momentum;      // 0..100
    private int hits, best, prevHurt;
    private long lastHit, popupAt, lastNs;
    private String popup = "";

    public MomentumModule() {
        super("Victus Momentum", Category.SPECIAL, 6, 60);
        w = 124; h = 30;
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (enabled && player == MinecraftClient.getInstance().player && entity instanceof LivingEntity) {
                momentum = Math.min(100, momentum + 11);
                hits++;
                best = Math.max(best, hits);
                lastHit = System.currentTimeMillis();
                if (popups.value && hits % 5 == 0) { popup = "x" + hits + " MOMENTUM!"; popupAt = lastHit; }
            }
            return ActionResult.PASS;
        });
    }

    @Override public void onTick(MinecraftClient mc) {
        if (mc.player == null) return;
        int hurt = mc.player.hurtTime;
        if (hurt > 0 && prevHurt == 0) { momentum *= 0.35; hits = 0; }
        prevHurt = hurt;
    }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        long now = System.nanoTime();
        double dt = lastNs == 0 ? 0.016 : Math.min(0.1, (now - lastNs) / 1e9);
        lastNs = now;
        if (System.currentTimeMillis() - lastHit > 1500) momentum = Math.max(0, momentum - decay.value * dt);
        if (momentum <= 0) hits = 0;

        w = 124; h = 30;
        float f = (float) (momentum / 100.0);
        boolean overdrive = momentum >= 75;
        float pulse = overdrive ? 0.6f + 0.4f * (float) Math.sin(System.currentTimeMillis() / 90.0) : 1f;

        if (background.value) UI.rrect(ctx, x, y, w, h, UI.alpha(0xFF0C0916, (float) (opacity.value / 100.0) + 0.25f));
        if (overdrive) UI.frame(ctx, x - 1, y - 1, w + 2, h + 2, UI.alpha(UI.ACCENT_LIGHT, pulse));

        String title = overdrive ? "OVERDRIVE" : "MOMENTUM";
        ctx.drawText(mc.textRenderer, title, x + 6, y + 5, overdrive ? UI.alpha(UI.ACCENT_LIGHT, 0.7f + 0.3f * pulse) : UI.ACCENT_LIGHT, shadow.value);
        String hs = "x" + hits;
        ctx.drawText(mc.textRenderer, hs, x + w - 6 - mc.textRenderer.getWidth(hs), y + 5, 0xFFFFFFFF, shadow.value);

        int bx = x + 6, by = y + 18, bw = w - 12;
        ctx.fill(bx, by, bx + bw, by + 6, 0xFF1E1840);
        int fillW = (int) (bw * f);
        if (fillW > 0) UI.hgrad(ctx, bx, by, fillW, 6, UI.ACCENT_DIM, overdrive ? UI.alpha(UI.ACCENT_LIGHT, pulse) : UI.ACCENT);
        if (fillW > 2) ctx.fill(bx + fillW - 2, by - 1, bx + fillW, by + 7, UI.alpha(0xFFFFFFFF, 0.8f)); // glowing tip

        long age = System.currentTimeMillis() - popupAt;
        if (popups.value && age < 1200) {
            float a = 1f - age / 1200f;
            int py = y - 12 - (int) ((1f - a) * 10);
            String t = popup;
            ctx.drawText(mc.textRenderer, t, x + (w - mc.textRenderer.getWidth(t)) / 2, py, UI.alpha(UI.ACCENT_LIGHT, Math.max(0.12f, a)), true);
        }
    }
}
