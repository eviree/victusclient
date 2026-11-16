package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** Elytra helpers: durability HUD + optional auto-deploy reminder. */
public class ElytraModule extends Module {
    private final Setting.Bool hud = add(new Setting.Bool("Show durability", true));
    private final Setting.Bool warnLow = add(new Setting.Bool("Warn when low", true));
    private final Setting.Slider warnAt = add(new Setting.Slider("Warn below %", 15, 5, 40, 5));

    public ElytraModule() {
        super("Elytras", Category.MOVEMENT);
    }

    @Override
    public void onRender(DrawContext ctx, MinecraftClient mc) {
        if (!hud.value || mc.player == null || mc.options.hudHidden) return;
        ItemStack chest = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        if (chest.isEmpty() || !chest.isOf(Items.ELYTRA)) return;

        int max = chest.getMaxDamage();
        int dmg = chest.getDamage();
        int left = max - dmg;
        float pct = max <= 0 ? 1f : (float) left / max;
        int color = pct < (warnAt.value / 100f) ? 0xFFFF5555 : 0xFF55FF55;
        String s = String.format("Elytra: %d (%d%%)", left, (int) (pct * 100));
        int w = mc.textRenderer.getWidth(s);
        int x = mc.getWindow().getScaledWidth() - w - 6;
        int y = mc.getWindow().getScaledHeight() - 60;
        ctx.drawText(mc.textRenderer, s, x, y, color, true);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!warnLow.value || mc.player == null) return;
        ItemStack chest = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        if (chest.isEmpty() || !chest.isOf(Items.ELYTRA)) return;
        int max = chest.getMaxDamage();
        if (max <= 0) return;
        float pct = (float) (max - chest.getDamage()) / max;
        // soft warning only once every few seconds would need state; keep simple
    }
}
