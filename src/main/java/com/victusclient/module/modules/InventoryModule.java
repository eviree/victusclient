package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

/** Shows your 27 main inventory slots on the HUD. */
public class InventoryModule extends HudModule {
    public InventoryModule() { super("Inventory", 6, 390); }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;
        w = 9 * 18 + 4; h = 3 * 18 + 4;
        if (background.value) ctx.fill(x, y, x + w, y + h, bgColor());
        for (int i = 0; i < 27; i++) {
            ItemStack s = mc.player.getInventory().getStack(9 + i);
            if (s.isEmpty()) continue;
            int sx = x + 2 + (i % 9) * 18 + 1, sy = y + 2 + (i / 9) * 18 + 1;
            ctx.drawItem(s, sx, sy);
            if (s.getCount() > 1) {
                String c = String.valueOf(s.getCount());
                ctx.drawText(mc.textRenderer, c, sx + 17 - mc.textRenderer.getWidth(c), sy + 9, 0xFFFFFFFF, true);
            }
        }
    }
}
