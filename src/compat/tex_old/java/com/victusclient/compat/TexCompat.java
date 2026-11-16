package com.victusclient.compat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/** Minecraft 1.21 - 1.21.1: drawTexture(Identifier, x, y, w, h, u, v, regionW, regionH, texW, texH). */
public final class TexCompat {
    private TexCompat() {}
    public static void draw(DrawContext ctx, Identifier tex, int x, int y, int w, int h, int texSize) {
        ctx.drawTexture(tex, x, y, w, h, 0f, 0f, texSize, texSize, texSize, texSize);
    }
}
