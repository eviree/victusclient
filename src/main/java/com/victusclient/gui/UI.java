package com.victusclient.gui;

import net.minecraft.client.gui.DrawContext;

/**
 * Victus / Unbox-inspired theme.
 * Dark panels, soft violet accent, rounded cards, clean hierarchy.
 */
public final class UI {
    public static final int BG = 0xFF08060F;
    public static final int PANEL_TOP = 0xFF12101C;
    public static final int PANEL_BOTTOM = 0xFF0A0812;
    public static final int SURFACE = 0xFF161322;
    public static final int SURFACE_HOVER = 0xFF1F1B30;
    public static final int SURFACE_ACTIVE = 0xFF252038;
    public static final int BORDER = 0xFF2A2540;
    public static final int BORDER_SOFT = 0xFF1E1A30;

    public static final int TEXT = 0xFFF2F0FF;
    public static final int MUTED = 0xFF8B85A8;
    public static final int MUTED2 = 0xFF6B6580;

    public static final int ACCENT = 0xFF8B6CFF;
    public static final int ACCENT_DIM = 0xFF3D2F7A;
    public static final int ACCENT_LIGHT = 0xFFC4B5FF;
    public static final int ACCENT_GLOW = 0x408B6CFF;

    public static final int GREEN = 0xFF34D399;
    public static final int RED = 0xFFEF4444;
    public static final int AMBER = 0xFFFBBF24;

    private UI() {}

    public static int alpha(int c, float a) {
        a = Math.max(0f, Math.min(1f, a));
        int al = (int) (((c >>> 24) & 0xFF) * a);
        return (al << 24) | (c & 0x00FFFFFF);
    }

    public static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF, aa = (a >>> 24) & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF, ba = (b >>> 24) & 0xFF;
        int rr = (int) (ar + (br - ar) * t);
        int rg = (int) (ag + (bg - ag) * t);
        int rb = (int) (ab + (bb - ab) * t);
        int ra = (int) (aa + (ba - aa) * t);
        return (ra << 24) | (rr << 16) | (rg << 8) | rb;
    }

    public static void rrect(DrawContext c, int x, int y, int w, int h, int col) {
        if (w <= 0 || h <= 0) return;
        c.fill(x + 2, y, x + w - 2, y + h, col);
        c.fill(x + 1, y + 1, x + w - 1, y + h - 1, col);
        c.fill(x, y + 2, x + w, y + h - 2, col);
    }

    public static void rrectSoft(DrawContext c, int x, int y, int w, int h, int col) {
        if (w <= 0 || h <= 0) return;
        c.fill(x + 3, y, x + w - 3, y + h, col);
        c.fill(x + 2, y + 1, x + w - 2, y + h - 1, col);
        c.fill(x + 1, y + 2, x + w - 1, y + h - 2, col);
        c.fill(x, y + 3, x + w, y + h - 3, col);
    }

    public static void card(DrawContext c, int x, int y, int w, int h, int fill, int border) {
        rrect(c, x, y, w, h, border);
        rrect(c, x + 1, y + 1, w - 2, h - 2, fill);
    }

    public static void cardElevated(DrawContext c, int x, int y, int w, int h, int fill, int border) {
        rrectSoft(c, x - 1, y - 1, w + 2, h + 2, alpha(ACCENT, 0.12f));
        card(c, x, y, w, h, fill, border);
    }

    public static void frame(DrawContext c, int x, int y, int w, int h, int col) {
        c.fill(x, y, x + w, y + 1, col);
        c.fill(x, y + h - 1, x + w, y + h, col);
        c.fill(x, y, x + 1, y + h, col);
        c.fill(x + w - 1, y, x + w, y + h, col);
    }

    public static void vgrad(DrawContext c, int x, int y, int w, int h, int top, int bottom) {
        int step = 2;
        for (int i = 0; i < h; i += step) {
            int bh = Math.min(step, h - i);
            c.fill(x, y + i, x + w, y + i + bh, lerp(top, bottom, h <= 1 ? 0f : (float) i / (h - 1)));
        }
    }

    public static void hgrad(DrawContext c, int x, int y, int w, int h, int left, int right) {
        int step = 2;
        for (int i = 0; i < w; i += step) {
            int bw = Math.min(step, w - i);
            c.fill(x + i, y, x + i + bw, y + h, lerp(left, right, w <= 1 ? 0f : (float) i / (w - 1)));
        }
    }

    public static void disc(DrawContext c, int cx, int cy, int r, int col) {
        for (int dy = -r; dy <= r; dy += 1) {
            int half = (int) Math.sqrt((double) r * r - (double) dy * dy);
            c.fill(cx - half, cy + dy, cx + half, cy + dy + 1, col);
        }
    }

    public static void slider(DrawContext c, int x, int y, int w, int h, float fraction, int track, int fill) {
        rrect(c, x, y, w, h, track);
        int fw = Math.max(0, (int) (w * Math.max(0f, Math.min(1f, fraction))));
        if (fw > 0) rrect(c, x, y, fw, h, fill);
    }
}
