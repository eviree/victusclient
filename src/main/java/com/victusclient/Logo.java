package com.victusclient;

import com.victusclient.compat.TexCompat;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class Logo {
    private Logo() {}
    public static final Identifier BIG = Identifier.of("victusclient", "textures/gui/logo.png");         // 256x256
    public static final Identifier SMALL = Identifier.of("victusclient", "textures/gui/logo_small.png"); // 128x128

    public static void draw(DrawContext ctx, int x, int y, int size) {
        if (size > 64) TexCompat.draw(ctx, BIG, x, y, size, size, 256);
        else TexCompat.draw(ctx, SMALL, x, y, size, size, 128);
    }
}
