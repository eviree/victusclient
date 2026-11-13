package com.victusclient.gui;

import com.victusclient.config.Config;
import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.module.ModuleManager;
import com.victusclient.setting.Setting;
import net.minecraft.client.gui.DrawContext;
import com.victusclient.compat.VScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

/** Victus Mod Menu: card grid, filter pills, favorites, toggle switches, settings page. */
public class ClickGUIScreen extends VScreen {
    private static class Hit {
        int x, y, w, h;
        Runnable left, right;
        Setting.Slider slider;
        boolean scroll;
        Hit(int x, int y, int w, int h) { this.x = x; this.y = y; this.w = w; this.h = h; }
        boolean in(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    private final List<Hit> hits = new ArrayList<>();
    private Hit dragging;
    private String search = "";
    private String filter = "All";
    private Module selected;
    private double scroll;
    private int maxScroll, viewTop, viewBottom;

    public ClickGUIScreen() { super(Text.literal("Victus Client")); }

    @Override public boolean shouldPause() { return false; }

    private boolean matches(Module m) {
        if (!search.isEmpty() && !m.name.toLowerCase().contains(search.toLowerCase())) return false;
        if (filter.equals("All")) return true;
        if (filter.equals("Favorites")) return m.favorite;
        return m.category.label.equals(filter);
    }

    private void toggleSwitch(DrawContext ctx, int x, int y, boolean on) {
        UI.rrect(ctx, x, y, 26, 12, on ? UI.ACCENT : 0xFF2A2A33);
        int kx = on ? x + 15 : x + 2;
        UI.rrect(ctx, kx, y + 2, 9, 8, on ? 0xFFFFFFFF : 0xFF6B6B76);
    }

    private void centered(DrawContext ctx, String s, int cx, int y, int color) {
        ctx.drawText(textRenderer, s, cx - textRenderer.getWidth(s) / 2, y, color, false);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        hits.clear();
        ctx.fill(0, 0, width, height, 0xC0000000);

        int pw = Math.min(width - 20, 440), ph = Math.min(height - 20, 290);
        int px = (width - pw) / 2, py = (height - ph) / 2;
        int headerH = 42, footerH = 28;

        UI.card(ctx, px, py, pw, ph, UI.BG, UI.BORDER);

        // Header: VICTUS (white) CLIENT (crimson) + muted label
        ctx.drawText(textRenderer, Text.literal("VICTUS").formatted(Formatting.BOLD), px + 14, py + 10, UI.TEXT, false);
        ctx.drawText(textRenderer, Text.literal("CLIENT").formatted(Formatting.BOLD),
                px + 14 + textRenderer.getWidth("VICTUS ") + 2, py + 10, UI.ACCENT, false);
        ctx.drawText(textRenderer, selected == null ? "MOD MENU" : selected.name.toUpperCase(), px + 14, py + 24, UI.MUTED, false);
        ctx.fill(px + 1, py + headerH, px + pw - 1, py + headerH + 1, UI.BORDER);

        if (selected == null) {
            List<String> filters = new ArrayList<>(List.of("All"));
            for (Category c : Category.values()) filters.add(c.label);
            filters.add("Favorites");
            int fx = px + pw - 12;
            for (int i = filters.size() - 1; i >= 0; i--) {
                String f = filters.get(i);
                int fw = textRenderer.getWidth(f) + 14;
                fx -= fw;
                Hit pill = new Hit(fx, py + 14, fw, 15);
                pill.left = () -> { filter = f; scroll = 0; };
                hits.add(pill);
                boolean sel = f.equals(filter);
                UI.card(ctx, pill.x, pill.y, fw, 15, sel ? UI.ACCENT : (pill.in(mx, my) ? UI.SURFACE_HOVER : UI.SURFACE),
                        sel ? UI.ACCENT : UI.BORDER);
                ctx.drawText(textRenderer, f, pill.x + 7, pill.y + 4, sel ? UI.TEXT : UI.MUTED, false);
                fx -= 5;
            }
        } else {
            Hit back = new Hit(px + pw - 66, py + 14, 54, 15);
            back.left = () -> { selected = null; scroll = 0; };
            hits.add(back);
            UI.card(ctx, back.x, back.y, 54, 15, back.in(mx, my) ? UI.SURFACE_HOVER : UI.SURFACE, UI.BORDER);
            ctx.drawText(textRenderer, "< Back", back.x + 10, back.y + 4, UI.TEXT, false);
        }

        int cx1 = px + 10, cx2 = px + pw - 10;
        viewTop = py + headerH + 9;
        viewBottom = py + ph - footerH - 4;
        ctx.enableScissor(px, viewTop, px + pw, viewBottom);
        int contentH = selected == null ? drawGrid(ctx, mx, my, cx1, cx2) : drawSettings(ctx, mx, my, cx1, cx2);
        ctx.disableScissor();
        maxScroll = Math.max(0, contentH - (viewBottom - viewTop));
        scroll = Math.max(0, Math.min(maxScroll, scroll));

        // Footer
        int fy = py + ph - footerH;
        ctx.fill(px + 1, fy, px + pw - 1, fy + 1, UI.BORDER);
        String s = search.isEmpty() ? "Search mods..." : search + ((System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
        ctx.drawText(textRenderer, s, px + 14, fy + 10, search.isEmpty() ? UI.MUTED : UI.TEXT, false);

        // White primary button (like LAUNCH GAME)
        Hit btn = new Hit(px + pw - 108, fy + 5, 96, 19);
        btn.left = () -> client.setScreen(new HudEditorScreen(this));
        hits.add(btn);
        UI.rrect(ctx, btn.x, btn.y, btn.w, btn.h, btn.in(mx, my) ? 0xFFE4E4E7 : 0xFFFFFFFF);
        centered(ctx, "HUD EDITOR", btn.x + btn.w / 2, btn.y + 6, 0xFF000000);
    }

    private int drawGrid(DrawContext ctx, int mx, int my, int x1, int x2) {
        int gap = 8, cw = (x2 - x1 - gap) / 2, ch = 42;
        int i = 0;
        for (Module m : ModuleManager.all()) {
            if (!matches(m)) continue;
            int cx = x1 + (i % 2) * (cw + gap);
            int cy = viewTop + (i / 2) * (ch + gap) - (int) scroll;
            i++;
            if (cy + ch < viewTop || cy > viewBottom) continue;

            Hit heart = new Hit(cx + cw - 22, cy + 3, 18, 14);
            heart.left = () -> m.favorite = !m.favorite;
            Hit sw = new Hit(cx + cw - 38, cy + ch - 22, 30, 18);
            sw.left = m::toggle;
            Hit card = new Hit(cx, cy, cw, ch);
            card.left = m.settings.isEmpty() ? m::toggle : () -> { selected = m; scroll = 0; };
            card.right = () -> { if (!m.settings.isEmpty()) { selected = m; scroll = 0; } };
            for (Hit h : new Hit[]{heart, sw, card}) { h.scroll = true; hits.add(h); }

            boolean hover = card.in(mx, my) && my >= viewTop && my <= viewBottom;
            UI.card(ctx, cx, cy, cw, ch, hover ? UI.SURFACE_HOVER : UI.SURFACE, m.enabled ? UI.ACCENT_DIM : UI.BORDER);

            // icon tile
            UI.card(ctx, cx + 7, cy + 7, 28, 28, 0xFF0A0A0D, m.enabled ? UI.ACCENT_DIM : UI.BORDER);
            String ini = m.name.substring(0, 1);
            ctx.drawText(textRenderer, ini, cx + 21 - textRenderer.getWidth(ini) / 2, cy + 17,
                    m.enabled ? UI.ACCENT : UI.MUTED, false);

            ctx.drawText(textRenderer, m.name, cx + 43, cy + 12, m.enabled ? UI.TEXT : 0xFFB4B4BE, false);
            ctx.drawText(textRenderer, m.enabled ? "ACTIVE" : "OFF", cx + 43, cy + 26,
                    m.enabled ? UI.GREEN : UI.MUTED, false);
            ctx.drawText(textRenderer, "\u2665", cx + cw - 15, cy + 6, m.favorite ? UI.ACCENT : 0xFF4A4A55, false);
            toggleSwitch(ctx, cx + cw - 36, cy + ch - 19, m.enabled);
        }
        int rows = (i + 1) / 2;
        return rows == 0 ? 0 : rows * (ch + gap) - gap;
    }

    private int drawSettings(DrawContext ctx, int mx, int my, int x1, int x2) {
        int w = x2 - x1, rh = 24, y = viewTop - (int) scroll, total = 0;
        Module m = selected;

        Hit en = new Hit(x1, y, w, rh);
        en.left = m::toggle; en.scroll = true; hits.add(en);
        UI.card(ctx, x1, y, w, rh, en.in(mx, my) ? UI.SURFACE_HOVER : UI.SURFACE, UI.BORDER);
        ctx.drawText(textRenderer, "Enabled", x1 + 10, y + 8, UI.TEXT, false);
        toggleSwitch(ctx, x2 - 36, y + 6, m.enabled);
        y += rh + 5; total += rh + 5;

        for (Setting st : m.settings) {
            Hit h = new Hit(x1, y, w, rh);
            h.scroll = true;
            UI.card(ctx, x1, y, w, rh, UI.SURFACE, UI.BORDER);
            if (st instanceof Setting.Bool b) {
                h.left = b::toggle;
                ctx.drawText(textRenderer, b.name, x1 + 10, y + 8, 0xFFD4D4DC, false);
                toggleSwitch(ctx, x2 - 36, y + 6, b.value);
            } else if (st instanceof Setting.Slider sl) {
                h.slider = sl;
                int fillW = (int) ((w - 2) * sl.fraction());
                if (fillW > 0) UI.rrect(ctx, x1 + 1, y + 1, Math.max(fillW, 4), rh - 2, UI.ACCENT_DIM);
                ctx.drawText(textRenderer, sl.name, x1 + 10, y + 8, UI.TEXT, false);
                String v = sl.display();
                ctx.drawText(textRenderer, v, x2 - 10 - textRenderer.getWidth(v), y + 8, UI.TEXT, false);
            } else if (st instanceof Setting.Mode md) {
                h.left = md::next; h.right = md::prev;
                ctx.drawText(textRenderer, md.name, x1 + 10, y + 8, 0xFFD4D4DC, false);
                String v = "< " + md.get() + " >";
                ctx.drawText(textRenderer, v, x2 - 10 - textRenderer.getWidth(v), y + 8, UI.ACCENT, false);
            }
            hits.add(h);
            y += rh + 5; total += rh + 5;
        }
        return total;
    }

    private void updateSlider(Hit h, double mx) { h.slider.setFraction((mx - h.x) / h.w); }

    // ---- input (Minecraft 1.21.9+ event objects) ----

    @Override
    protected boolean onClick(double mx, double my, int button) {
        for (Hit h : hits) {
            if (!h.in(mx, my)) continue;
            if (h.scroll && (my < viewTop || my > viewBottom)) continue;
            if (button == 0 && h.slider != null) { dragging = h; updateSlider(h, mx); }
            else if (button == 0 && h.left != null) h.left.run();
            else if (button == 1 && h.right != null) h.right.run();
            return true;
        }
        return false;
    }

    @Override
    protected boolean onDrag(double mx, double my, int button) {
        if (dragging != null) { updateSlider(dragging, mx); return true; }
        return false;
    }

    @Override
    protected boolean onRelease(double mx, double my, int button) {
        dragging = null;
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        scroll = Math.max(0, Math.min(maxScroll, scroll - vertical * 20));
        return true;
    }

    @Override
    protected boolean onKey(int key) {
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        if (key == GLFW.GLFW_KEY_ESCAPE && selected != null) { selected = null; scroll = 0; return true; }
        if (key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onChar(int cp) {
        if (cp >= 32 && cp != 127 && search.length() < 20) {
            search += new String(Character.toChars(cp));
            selected = null; scroll = 0;
            return true;
        }
        return false;
    }

    @Override public void removed() { Config.save(); }
}
