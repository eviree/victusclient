package com.victusclient.gui;

import net.minecraft.client.gui.DrawContext;

/** Victus violet theme + small drawing helpers (fills only, so they work on every Minecraft version). */
public final class UI {
    public static final int BG = 0xFF0C0916;
    public static final int PANEL_TOP = 0xFF16112E;
    public static final int PANEL_BOTTOM = 0xFF0B0817;
    public static final int SURFACE = 0xFF171230;
    public static final int SURFACE_HOVER = 0xFF221A48;
    public static final int BORDER = 0xFF2C2354;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int MUTED = 0xFF9A93C4;
    public static final int ACCENT = 0xFF7C5CFF;
    public static final int ACCENT_DIM = 0xFF3B2A8C;
    public static final int ACCENT_LIGHT = 0xFFB8A9FF;
    public static final int GREEN = 0xFF34D399;

    private UI() {}

    public static int alpha(int c, float a) {
        a = Math.max(0f, Math.min(1f, a));
        int al = (int) (((c >>> 24) & 0xFF) * a);
        return (al << 24) | (c & 0xFFFFFF);
    }

    public static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (int) (((a >>> 24) & 0xFF) + (((b >>> 24) & 0xFF) - ((a >>> 24) & 0xFF)) * t);
        int ar = (int) (((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t);
        int ag = (int) (((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t);
        int ab = (int) ((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
        return (aa << 24) | (ar << 16) | (ag << 8) | ab;
    }

    public static void rrect(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x + 2, y, x + w - 2, y + h, col);
        c.fill(x + 1, y + 1, x + w - 1, y + h - 1, col);
        c.fill(x, y + 2, x + w, y + h - 2, col);
    }

    public static void card(DrawContext c, int x, int y, int w, int h, int fill, int border) {
        rrect(c, x, y, w, h, border);
        rrect(c, x + 1, y + 1, w - 2, h - 2, fill);
    }

    public static void frame(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x, y, x + w, y + 1, col);
        c.fill(x, y + h - 1, x + w, y + h, col);
        c.fill(x, y, x + 1, y + h, col);
        c.fill(x + w - 1, y, x + w, y + h, col);
    }

    /** Vertical gradient built from thin bands. */
    public static void vgrad(DrawContext c, int x, int y, int w, int h, int top, int bottom) {
        int step = 3;
        for (int i = 0; i < h; i += step) {
            int bh = Math.min(step, h - i);
            c.fill(x, y + i, x + w, y + i + bh, lerp(top, bottom, h <= 1 ? 0f : (float) i / (h - 1)));
        }
    }

    /** Horizontal gradient built from thin bands. */
    public static void hgrad(DrawContext c, int x, int y, int w, int h, int left, int right) {
        int step = 3;
        for (int i = 0; i < w; i += step) {
            int bw = Math.min(step, w - i);
            c.fill(x + i, y, x + i + bw, y + h, lerp(left, right, w <= 1 ? 0f : (float) i / (w - 1)));
        }
    }

    /** Filled disc using scanlines (every 2nd row, for speed). */
    public static void disc(DrawContext c, int cx, int cy, int r, int col) {
        for (int dy = -r; dy <= r; dy += 2) {
            int half = (int) Math.sqrt((double) r * r - (double) dy * dy);
            c.fill(cx - half, cy + dy, cx + half, cy + dy + 2, col);
        }
    }
}
