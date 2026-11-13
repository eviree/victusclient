package com.victusclient.gui;

import net.minecraft.client.gui.DrawContext;

/** Victus launcher theme: near-black surfaces, crimson accent, white primary buttons. */
public final class UI {
    public static final int BG = 0xFF09090B;
    public static final int SURFACE = 0xFF0F0F13;
    public static final int SURFACE_HOVER = 0xFF17171D;
    public static final int BORDER = 0xFF23232B;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int MUTED = 0xFF8A8A96;
    public static final int ACCENT = 0xFFE11D48;
    public static final int ACCENT_DIM = 0xFF6B1226;
    public static final int GREEN = 0xFF34D399;

    private UI() {}

    /** Filled rect with small rounded corners. */
    public static void rrect(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x + 2, y, x + w - 2, y + h, col);
        c.fill(x + 1, y + 1, x + w - 1, y + h - 1, col);
        c.fill(x, y + 2, x + w, y + h - 2, col);
    }

    /** Rounded card with 1px border (opaque colors). */
    public static void card(DrawContext c, int x, int y, int w, int h, int fill, int border) {
        rrect(c, x, y, w, h, border);
        rrect(c, x + 1, y + 1, w - 2, h - 2, fill);
    }

    /** Plain 1px rectangle outline. */
    public static void frame(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x, y, x + w, y + 1, col);
        c.fill(x, y + h - 1, x + w, y + h, col);
        c.fill(x, y, x + 1, y + h, col);
        c.fill(x + w - 1, y, x + w, y + h, col);
    }
}
