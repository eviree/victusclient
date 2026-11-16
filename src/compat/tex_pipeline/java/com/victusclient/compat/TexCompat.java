package com.victusclient.compat;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/** Minecraft 1.21.6+: drawTexture(RenderPipeline, Identifier, ...). */
public final class TexCompat {
    private TexCompat() {}
    public static void draw(DrawContext ctx, Identifier tex, int x, int y, int w, int h, int texSize) {
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, tex, x, y, 0f, 0f, w, h, texSize, texSize, texSize, texSize);
    }
}
