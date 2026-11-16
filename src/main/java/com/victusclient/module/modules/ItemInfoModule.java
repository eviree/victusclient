package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class ItemInfoModule extends HudModule {
    public ItemInfoModule() { super("Item Info", 6, 450); }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null) return;
        ItemStack s = mc.player.getMainHandStack();
        List<String> ls = new ArrayList<>();
        if (!s.isEmpty()) {
            ls.add(s.getName().getString());
            if (s.isDamageable() && s.getMaxDamage() > 0)
                ls.add("Durability: " + (s.getMaxDamage() - s.getDamage()) + " / " + s.getMaxDamage());
            if (s.getCount() > 1) ls.add("Count: " + s.getCount());
        }
        lines(ctx, mc, ls);
    }
}
