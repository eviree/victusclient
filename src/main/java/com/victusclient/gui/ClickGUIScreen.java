package com.victusclient.gui;

import com.victusclient.GlobalSettings;
import com.victusclient.Logo;
import com.victusclient.VictusClient;
import com.victusclient.compat.VScreen;
import com.victusclient.config.Config;
import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.module.ModuleManager;
import com.victusclient.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Victus Mod Menu: sidebar tabs, module list with animated switches, info panel with settings. */
public class ClickGUIScreen extends VScreen {
    private static class Hit {
        int x, y, w, h, zone; // zone 0 = chrome, 1 = list (clipped), 2 = info panel (clipped)
        Runnable left, right;
        Setting.Slider slider;
        Hit(int x, int y, int w, int h, int zone) { this.x = x; this.y = y; this.w = w; this.h = h; this.zone = zone; }
        boolean in(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    private static final int TAB_FAV = 7, TAB_PROFILES = 8, TAB_SETTINGS = 9;
    private static int rememberedTab = 2;

    private final List<Hit> hits = new ArrayList<>();
    private final Map<Setting, Float> switchAnim = new HashMap<>();
    private Hit dragging;
    private String search = "";
    private int tab = rememberedTab;
    private Module selected;
    private double listScroll, listScrollT, infoScroll, infoScrollT;
    private int listMax, infoMax, lt, lb, infoX, infoW;
    private float fade, infoFade = 1f, indicatorY = -1f, dt;
    private long lastNs, toastAt;
    private boolean anim = true;
    private String toast = "";

    public ClickGUIScreen() { super(Text.literal("Victus Client")); }

    @Override public boolean shouldPause() { return false; }

    // ---------------- helpers ----------------
    private float ease(float rate) { return anim ? 1f - (float) Math.exp(-dt * rate) : 1f; }
    private int c(int col) { return UI.alpha(col, fade); }

    private void text(DrawContext ctx, String s, int x, int y, int col) {
        ctx.drawText(textRenderer, s, x, y, c(col), false);
    }

    private void textCentered(DrawContext ctx, String s, int cx, int y, int col) {
        ctx.drawText(textRenderer, s, cx - textRenderer.getWidth(s) / 2, y, c(col), false);
    }

    private int wrap(DrawContext ctx, String s, int x, int y, int w, int col) {
        for (OrderedText line : textRenderer.wrapLines(Text.literal(s), w)) {
            ctx.drawText(textRenderer, line, x, y, c(col), false);
            y += 10;
        }
        return y;
    }

    private String clip(String s, int w) {
        if (textRenderer.getWidth(s) <= w) return s;
        return textRenderer.trimToWidth(s, Math.max(0, w - textRenderer.getWidth("..."))) + "...";
    }

    private static String version() {
        return FabricLoader.getInstance().getModContainer(VictusClient.ID)
                .map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("dev");
    }

    private void toast(String s) { toast = s; toastAt = System.currentTimeMillis(); }

    private void select(Module m) { selected = m; infoScrollT = 0; infoScroll = 0; infoFade = 0f; }

    private void switchTrack(DrawContext ctx, int x, int y, int w, int h, float t) {
        UI.rrect(ctx, x, y, w, h, c(UI.lerp(0xFF2B2550, UI.ACCENT, t)));
        int kn = h - 4;
        int kx = x + 2 + (int) ((w - kn - 4) * t);
        UI.rrect(ctx, kx, y + 2, kn, kn, c(0xFFFFFFFF));
    }

    private List<Module> visible() {
        List<Module> out = new ArrayList<>();
        boolean searching = !search.isEmpty();
        for (Module m : ModuleManager.all()) {
            if (searching) { if (!m.name.toLowerCase().contains(search.toLowerCase())) continue; }
            else if (tab == TAB_FAV) { if (!m.favorite) continue; }
            else if (tab <= 6) { if (m.category != Category.values()[tab]) continue; }
            else continue;
            out.add(m);
        }
        out.sort(Comparator.comparing(m -> m.soon)); // working modules first
        return out;
    }

    // ---------------- render ----------------
    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        long now = System.nanoTime();
        dt = lastNs == 0 ? 0.016f : Math.min(0.05f, (now - lastNs) / 1.0e9f);
        lastNs = now;
        anim = GlobalSettings.animations.value;
        fade = anim ? fade + (1f - fade) * ease(11f) : 1f;
        if (fade > 0.995f) fade = 1f;
        infoFade += (1f - infoFade) * ease(14f);
        listScroll += (listScrollT - listScroll) * ease(20f);
        infoScroll += (infoScrollT - infoScroll) * ease(20f);
        hits.clear();

        ctx.fill(0, 0, width, height, UI.alpha(0xD0050310, fade));
        if (fade < 0.03f) return;

        int pw = Math.min(width - 8, 470), ph = Math.min(height - 8, 300);
        int px = (width - pw) / 2, py = (height - ph) / 2 + (int) ((1f - fade) * 14);
        float panelA = fade * (float) (GlobalSettings.menuOpacity.value / 100.0);
        int hh = 44, fh = 26;

        // glow + panel
        for (int i = 3; i >= 1; i--) UI.rrect(ctx, px - i, py - i, pw + 2 * i, ph + 2 * i, UI.alpha(UI.ACCENT, fade * 0.10f * (4 - i)));
        UI.rrect(ctx, px, py, pw, ph, c(0xFF3A2D78));
        UI.vgrad(ctx, px + 1, py + 2, pw - 2, ph - 4, UI.alpha(UI.PANEL_TOP, panelA), UI.alpha(UI.PANEL_BOTTOM, panelA));

        // ---------- header ----------
        if (fade > 0.5f) Logo.draw(ctx, px + 10, py + 6, 32);
        Text vic = Text.literal("VICTUS").formatted(Formatting.BOLD);
        ctx.drawText(textRenderer, vic, px + 48, py + 11, c(UI.TEXT), false);
        ctx.drawText(textRenderer, Text.literal("CLIENT").formatted(Formatting.BOLD),
                px + 48 + textRenderer.getWidth(vic) + 4, py + 11, c(UI.ACCENT_LIGHT), false);
        text(ctx, "v" + version(), px + 48, py + 24, UI.MUTED);

        int right = px + pw - 10;
        // close
        Hit close = new Hit(right - 18, py + 13, 18, 18, 0);
        close.left = this::close;
        hits.add(close);
        UI.card(ctx, close.x, close.y, 18, 18, c(close.in(mx, my) ? 0xFF5A2A4A : UI.SURFACE), c(UI.BORDER));
        textCentered(ctx, "x", close.x + 9, close.y + 5, UI.TEXT);
        // settings (slider icon)
        Hit gear = new Hit(right - 42, py + 13, 18, 18, 0);
        gear.left = () -> { tab = TAB_SETTINGS; search = ""; listScrollT = 0; select(null); };
        hits.add(gear);
        UI.card(ctx, gear.x, gear.y, 18, 18, c(gear.in(mx, my) ? UI.SURFACE_HOVER : UI.SURFACE), c(UI.BORDER));
        for (int i = 0; i < 3; i++) {
            ctx.fill(gear.x + 4, gear.y + 5 + i * 4, gear.x + 14, gear.y + 6 + i * 4, c(UI.MUTED));
            ctx.fill(gear.x + 4 + (i * 4 + 2) % 9, gear.y + 4 + i * 4, gear.x + 7 + (i * 4 + 2) % 9, gear.y + 7 + i * 4, c(UI.ACCENT_LIGHT));
        }
        // profile chip
        Hit chip = new Hit(right - 108, py + 13, 62, 18, 0);
        chip.left = () -> { tab = TAB_PROFILES; search = ""; listScrollT = 0; select(null); };
        hits.add(chip);
        UI.card(ctx, chip.x, chip.y, 62, 18, c(chip.in(mx, my) ? UI.SURFACE_HOVER : UI.SURFACE), c(UI.BORDER));
        text(ctx, clip(Config.profile, 44) + " v", chip.x + 7, chip.y + 5, UI.TEXT);
        // search
        int sx0 = px + 150, sw0 = chip.x - 8 - sx0;
        if (sw0 > 40) {
            UI.card(ctx, sx0, py + 13, sw0, 18, c(UI.SURFACE), c(search.isEmpty() ? UI.BORDER : UI.ACCENT_DIM));
            String ss = search.isEmpty() ? "Search modules..." : search + ((System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
            text(ctx, clip(ss, sw0 - 14), sx0 + 7, py + 18, search.isEmpty() ? UI.MUTED : UI.TEXT);
        }
        ctx.fill(px + 1, py + hh, px + pw - 1, py + hh + 1, c(UI.BORDER));

        // ---------- sidebar ----------
        int sbX = px + 8, sbW = 92, sbTop = py + hh + 8;
        int rh = Math.max(15, Math.min(23, (ph - hh - 8 - fh - 6) / 10));
        String[] labels = new String[10];
        for (int i = 0; i < 7; i++) labels[i] = Category.values()[i].label;
        labels[TAB_FAV] = "Favorites"; labels[TAB_PROFILES] = "Profiles"; labels[TAB_SETTINGS] = "Settings";
        float targetY = sbTop + tab * rh + (tab >= 7 ? 6 : 0);
        indicatorY = indicatorY < 0 ? targetY : indicatorY + (targetY - indicatorY) * ease(16f);
        UI.rrect(ctx, sbX, (int) indicatorY, sbW, rh - 2, c(UI.ACCENT_DIM));
        ctx.fill(sbX, (int) indicatorY + 2, sbX + 2, (int) indicatorY + rh - 4, c(UI.ACCENT_LIGHT));
        for (int i = 0; i < 10; i++) {
            int ry = sbTop + i * rh + (i >= 7 ? 6 : 0);
            if (i == 7) ctx.fill(sbX + 4, ry - 4, sbX + sbW - 4, ry - 3, c(UI.BORDER));
            final int idx = i;
            Hit h = new Hit(sbX, ry, sbW, rh - 2, 0);
            h.left = () -> { tab = idx; search = ""; listScrollT = 0; listScroll = 0; select(null); rememberedTab = idx; };
            hits.add(h);
            boolean active = i == tab, hov = h.in(mx, my);
            if (hov && !active) UI.rrect(ctx, sbX, ry, sbW, rh - 2, c(UI.alpha(UI.SURFACE_HOVER, 0.8f)));
            text(ctx, labels[i], sbX + 10, ry + (rh - 2 - 8) / 2, active ? UI.TEXT : (hov ? UI.ACCENT_LIGHT : UI.MUTED));
        }
        text(ctx, "Victus Client", sbX + 4, py + ph - fh - 6, UI.MUTED);
        ctx.fill(px + 104, py + hh + 6, px + 105, py + ph - fh, c(UI.BORDER));

        // ---------- content ----------
        int listX = px + 112;
        infoW = 130;
        infoX = px + pw - 8 - infoW;
        int listW = infoX - 8 - listX;
        lt = py + hh + 8;
        lb = py + ph - fh - 2;

        ctx.enableScissor(listX - 1, lt, listX + listW + 1, lb);
        int contentH;
        if (tab == TAB_PROFILES && search.isEmpty()) contentH = drawProfiles(ctx, mx, my, listX, listW);
        else if (tab == TAB_SETTINGS && search.isEmpty()) contentH = drawGlobal(ctx, mx, my, listX, listW);
        else contentH = drawModules(ctx, mx, my, listX, listW);
        ctx.disableScissor();
        listMax = Math.max(0, contentH - (lb - lt));
        listScrollT = Math.max(0, Math.min(listMax, listScrollT));

        drawInfo(ctx, mx, my, infoX, lt, infoW, lb - lt);

        // ---------- footer ----------
        int fy = py + ph - fh;
        ctx.fill(px + 1, fy, px + pw - 1, fy + 1, c(UI.BORDER));
        boolean showToast = System.currentTimeMillis() - toastAt < 2200;
        text(ctx, showToast ? toast : "RShift to close  |  type to search", px + 12, fy + 9, showToast ? UI.ACCENT_LIGHT : UI.MUTED);
        Hit btn = new Hit(px + pw - 104, fy + 4, 94, 18, 0);
        btn.left = () -> client.setScreen(new HudEditorScreen(this));
        hits.add(btn);
        UI.rrect(ctx, btn.x, btn.y, btn.w, btn.h, c(btn.in(mx, my) ? 0xFFE2DEFF : 0xFFFFFFFF));
        ctx.drawText(textRenderer, "HUD EDITOR", btn.x + (btn.w - textRenderer.getWidth("HUD EDITOR")) / 2, btn.y + 5, c(0xFF120C2C), false);
    }

    private int drawModules(DrawContext ctx, int mx, int my, int lx, int lw) {
        List<Module> mods = visible();
        int rowH = 32, gap = 4;
        if (mods.isEmpty()) {
            String msg = !search.isEmpty() ? "No modules found." : (tab == TAB_FAV ? "Click the heart on a module to add it here." : "Nothing here yet.");
            text(ctx, msg, lx + 4, lt + 6, UI.MUTED);
            return 0;
        }
        int y = lt - (int) listScroll;
        for (Module m : mods) {
            int ry = y;
            y += rowH + gap;
            if (ry + rowH < lt || ry > lb) continue;

            Hit row = new Hit(lx, ry, lw, rowH, 1);
            row.left = () -> select(m);
            row.right = () -> { if (!m.soon) m.toggle(); };
            hits.add(row);
            boolean hover = row.in(mx, my) && my >= lt && my <= lb;
            m.uiHover += ((hover ? 1f : 0f) - m.uiHover) * ease(16f);
            m.uiToggle += ((m.enabled ? 1f : 0f) - m.uiToggle) * ease(18f);
            boolean sel = m == selected;

            UI.card(ctx, lx, ry, lw, rowH, c(UI.lerp(UI.SURFACE, UI.SURFACE_HOVER, m.uiHover)),
                    c(sel ? UI.ACCENT : UI.lerp(UI.BORDER, UI.ACCENT_DIM, m.uiToggle)));
            // icon tile
            UI.card(ctx, lx + 6, ry + 5, 22, 22, c(0xFF100C26), c(UI.lerp(UI.BORDER, UI.ACCENT_DIM, m.uiToggle)));
            String ini = m.name.substring(0, 1);
            ctx.drawText(textRenderer, ini, lx + 17 - textRenderer.getWidth(ini) / 2, ry + 12,
                    c(m.soon ? 0xFF5A5380 : UI.lerp(UI.MUTED, UI.ACCENT_LIGHT, m.uiToggle)), false);
            // texts
            text(ctx, clip(m.name, lw - 34 - 20), lx + 34, ry + 7, m.soon ? UI.MUTED : UI.TEXT);
            text(ctx, clip(m.description, lw - 34 - 38), lx + 34, ry + 19, m.soon ? 0xFF5F5885 : UI.MUTED);

            if (m.soon) {
                UI.card(ctx, lx + lw - 36, ry + 17, 30, 11, c(0xFF15102C), c(UI.BORDER));
                ctx.drawText(textRenderer, "SOON", lx + lw - 21 - textRenderer.getWidth("SOON") / 2, ry + 19, c(UI.MUTED), false);
            } else {
                Hit heart = new Hit(lx + lw - 22, ry + 2, 18, 13, 1);
                heart.left = () -> m.favorite = !m.favorite;
                hits.add(heart);
                text(ctx, "\u2665", lx + lw - 14, ry + 5, m.favorite ? UI.ACCENT_LIGHT : 0xFF4A4378);
                Hit sw = new Hit(lx + lw - 40, ry + 15, 36, 15, 1);
                sw.left = m::toggle;
                hits.add(sw);
                switchTrack(ctx, lx + lw - 34, ry + 17, 28, 11, m.uiToggle);
            }
        }
        return mods.size() * (rowH + gap) - gap;
    }

    private int drawProfiles(DrawContext ctx, int mx, int my, int lx, int lw) {
        int rowH = 32, gap = 4, y = lt - (int) listScroll;
        for (String name : Config.PROFILES) {
            int ry = y;
            y += rowH + gap;
            boolean active = name.equals(Config.profile);
            UI.card(ctx, lx, ry, lw, rowH, c(UI.SURFACE), c(active ? UI.ACCENT : UI.BORDER));
            UI.card(ctx, lx + 6, ry + 5, 22, 22, c(0xFF100C26), c(active ? UI.ACCENT_DIM : UI.BORDER));
            ctx.drawText(textRenderer, name.substring(0, 1), lx + 17 - textRenderer.getWidth(name.substring(0, 1)) / 2, ry + 12,
                    c(active ? UI.ACCENT_LIGHT : UI.MUTED), false);
            text(ctx, name, lx + 34, ry + 7, UI.TEXT);
            text(ctx, active ? "ACTIVE" : "Saved layout", lx + 34, ry + 19, active ? UI.GREEN : UI.MUTED);

            Hit load = new Hit(lx + lw - 40, ry + 8, 34, 16, 1);
            load.left = () -> { Config.profile = name; Config.loadProfile(name); toast("Loaded profile: " + name); };
            Hit save = new Hit(lx + lw - 80, ry + 8, 34, 16, 1);
            save.left = () -> { Config.saveProfile(name); toast("Saved profile: " + name); };
            hits.add(save); hits.add(load);
            for (Object[] b : new Object[][]{{save, "Save"}, {load, "Load"}}) {
                Hit hh2 = (Hit) b[0];
                UI.card(ctx, hh2.x, hh2.y, hh2.w, hh2.h, c(hh2.in(mx, my) && my >= lt && my <= lb ? UI.ACCENT_DIM : 0xFF1C1640), c(UI.BORDER));
                ctx.drawText(textRenderer, (String) b[1], hh2.x + (hh2.w - textRenderer.getWidth((String) b[1])) / 2, hh2.y + 4, c(UI.TEXT), false);
            }
        }
        return Config.PROFILES.length * (rowH + gap) - gap;
    }

    private int drawGlobal(DrawContext ctx, int mx, int my, int lx, int lw) {
        int rh = 26, gap = 4, y = lt - (int) listScroll;
        for (Setting st : GlobalSettings.ALL) {
            settingRow(ctx, st, lx, y, lw, rh, 1, mx, my);
            y += rh + gap;
        }
        return GlobalSettings.ALL.size() * (rh + gap) - gap;
    }

    /** One setting row (bool / slider / mode), used by the Settings tab and the info panel. */
    private void settingRow(DrawContext ctx, Setting st, int x, int y, int w, int rh, int zone, int mx, int my) {
        int top = lt, bottom = lb;
        if (y + rh < top || y > bottom) return;
        Hit h = new Hit(x, y, w, rh, zone);
        boolean hover = h.in(mx, my) && my >= top && my <= bottom;
        UI.card(ctx, x, y, w, rh, c(hover ? UI.SURFACE_HOVER : UI.SURFACE), c(UI.BORDER));
        int ty = y + (rh - 8) / 2;
        if (st instanceof Setting.Bool b) {
            h.left = b::toggle;
            float a = switchAnim.getOrDefault(st, b.value ? 1f : 0f);
            a += ((b.value ? 1f : 0f) - a) * ease(18f);
            switchAnim.put(st, a);
            text(ctx, clip(b.name, w - 44), x + 8, ty, 0xFFD9D4F5);
            switchTrack(ctx, x + w - 32, y + (rh - 11) / 2, 24, 11, a);
        } else if (st instanceof Setting.Slider sl) {
            h.slider = sl;
            int fillW = (int) ((w - 2) * sl.fraction());
            if (fillW > 0) UI.rrect(ctx, x + 1, y + 1, Math.max(fillW, 4), rh - 2, c(UI.ACCENT_DIM));
            text(ctx, clip(sl.name, w - 40), x + 8, ty, UI.TEXT);
            String v = sl.display();
            text(ctx, v, x + w - 8 - textRenderer.getWidth(v), ty, UI.TEXT);
        } else if (st instanceof Setting.Mode md) {
            h.left = md::next; h.right = md::prev;
            text(ctx, clip(md.name, w / 2 - 6), x + 8, ty, 0xFFD9D4F5);
            String v = "< " + md.get() + " >";
            text(ctx, v, x + w - 8 - textRenderer.getWidth(v), ty, UI.ACCENT_LIGHT);
        }
        hits.add(h);
    }

    private void drawInfo(DrawContext ctx, int mx, int my, int ix, int iy, int iw, int ih) {
        UI.card(ctx, ix, iy, iw, ih, c(0xFF110D26), c(UI.BORDER));
        ctx.enableScissor(ix + 1, iy + 1, ix + iw - 1, iy + ih - 1);
        int x = ix + 8, w = iw - 16;
        int start = iy + 8 - (int) infoScroll, y = start;

        if (fade > 0.5f) Logo.draw(ctx, ix + (iw - 44) / 2, y, 44);
        y += 48;
        Text brand = Text.literal("VICTUS CLIENT").formatted(Formatting.BOLD);
        ctx.drawText(textRenderer, brand, ix + (iw - textRenderer.getWidth(brand)) / 2, y, c(UI.TEXT), false);
        y += 11;
        for (OrderedText line : textRenderer.wrapLines(Text.literal("MORE THAN JUST A CLIENT"), w)) {
            ctx.drawText(textRenderer, line, ix + (iw - textRenderer.getWidth(line)) / 2, y, c(UI.MUTED), false);
            y += 10;
        }
        y += 4;
        ctx.fill(ix + 8, y, ix + iw - 8, y + 1, c(UI.BORDER));
        y += 7;
        ctx.drawText(textRenderer, Text.literal("Module Info").formatted(Formatting.BOLD), x, y, c(UI.ACCENT_LIGHT), false);
        y += 14;

        float prev = fade;
        fade = prev * infoFade; // fade-in of the selected module's details
        if (tab == TAB_PROFILES && search.isEmpty()) {
            y = wrap(ctx, "Profiles store which mods are on, their settings and HUD positions. Save a layout, load it any time.", x, y, w, UI.MUTED);
        } else if (tab == TAB_SETTINGS && search.isEmpty()) {
            y = wrap(ctx, "Client-wide settings for animations, the title screen and menu opacity.", x, y, w, UI.MUTED);
        } else if (selected == null) {
            y = wrap(ctx, "Select a module to view more information here.", x, y, w, UI.MUTED);
        } else {
            Module m = selected;
            ctx.drawText(textRenderer, Text.literal(m.name).formatted(Formatting.BOLD), x, y, c(UI.TEXT), false);
            y += 12;
            text(ctx, m.soon ? "COMING SOON" : (m.enabled ? "ENABLED" : "DISABLED"), x, y,
                    m.soon ? UI.MUTED : (m.enabled ? UI.GREEN : 0xFFE27A8C));
            y += 13;
            y = wrap(ctx, m.description, x, y, w, 0xFFD9D4F5) + 4;
            if (m.soon) {
                y = wrap(ctx, "On the Victus roadmap. This one needs deeper game hooks and ships in a future update.", x, y, w, UI.MUTED);
            } else if (m.settings.isEmpty()) {
                y = wrap(ctx, "No settings.", x, y, w, UI.MUTED);
            } else {
                ctx.drawText(textRenderer, Text.literal("Settings").formatted(Formatting.BOLD), x, y, c(UI.ACCENT_LIGHT), false);
                y += 13;
                for (Setting st : m.settings) {
                    settingRowInfo(ctx, st, ix + 6, y, iw - 12, 20, mx, my, iy, ih);
                    y += 24;
                }
            }
        }
        fade = prev;
        ctx.disableScissor();
        int contentH = y - start + 8;
        infoMax = Math.max(0, contentH - (ih - 4));
        infoScrollT = Math.max(0, Math.min(infoMax, infoScrollT));
    }

    /** Same as settingRow but clipped to the info panel instead of the list. */
    private void settingRowInfo(DrawContext ctx, Setting st, int x, int y, int w, int rh, int mx, int my, int iy, int ih) {
        int saveTop = lt, saveBottom = lb;
        lt = iy; lb = iy + ih;
        settingRow(ctx, st, x, y, w, rh, 2, mx, my);
        lt = saveTop; lb = saveBottom;
    }

    // ---------------- input ----------------
    private boolean clipOk(Hit h, double mx, double my) {
        if (h.zone == 1) return my >= lt && my <= lb;
        if (h.zone == 2) return my >= lt && my <= lb && mx >= infoX && mx <= infoX + infoW;
        return true;
    }

    private void updateSlider(Hit h, double mx) { h.slider.setFraction((mx - h.x) / h.w); }

    @Override
    protected boolean onClick(double mx, double my, int button) {
        for (int i = hits.size() - 1; i >= 0; i--) { // top-most first
            Hit h = hits.get(i);
            if (!h.in(mx, my) || !clipOk(h, mx, my)) continue;
            if (button == 0 && h.slider != null) { dragging = h; updateSlider(h, mx); }
            else if (button == 0 && h.left != null) h.left.run();
            else if (button == 1 && h.right != null) h.right.run();
            else continue;
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
        if (mx >= infoX) infoScrollT = Math.max(0, Math.min(infoMax, infoScrollT - vertical * 22));
        else listScrollT = Math.max(0, Math.min(listMax, listScrollT - vertical * 26));
        return true;
    }

    @Override
    protected boolean onKey(int key) {
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        if (key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            listScrollT = 0; listScroll = 0;
            return true;
        }
        return false;
    }

    @Override
    protected boolean onChar(int cp) {
        if (cp >= 32 && cp != 127 && search.length() < 24) {
            search += new String(Character.toChars(cp));
            listScrollT = 0; listScroll = 0;
            return true;
        }
        return false;
    }

    @Override public void removed() { Config.save(); }
}
