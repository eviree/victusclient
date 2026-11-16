package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class BlockIndicatorModule extends HudModule {
    public BlockIndicatorModule() { super("Block Indicator", 6, 350); }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        if (mc.world == null) return;
        HitResult hr = mc.crosshairTarget;
        if (hr instanceof BlockHitResult b && hr.getType() == HitResult.Type.BLOCK) {
            String n = mc.world.getBlockState(b.getBlockPos()).getBlock().getName().getString();
            box(ctx, mc, "Looking at: " + n);
        } else {
            box(ctx, mc, "Looking at: --");
        }
    }
}
