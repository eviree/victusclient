package com.victusclient.compat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

/** Minecraft 1.21.2 - 1.21.5: drawTexture(Function<Identifier, RenderLayer>, Identifier, ...). */
public final class TexCompat {
    private TexCompat() {}
    public static void draw(DrawContext ctx, Identifier tex, int x, int y, int w, int h, int texSize) {
        ctx.drawTexture(RenderLayer::getGuiTextured, tex, x, y, 0f, 0f, w, h, texSize, texSize, texSize, texSize);
    }
}
