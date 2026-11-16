package com.victusclient.gui;

import com.victusclient.GlobalSettings;
import com.victusclient.Logo;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Applies the Victus purple animated chrome to vanilla menus:
 * Select World, Multiplayer, Options, Create World, and the in-game pause menu.
 * Draws an overlay skin (header V logo, purple buttons, soft animations)
 * on top of the real widgets so all click behavior stays vanilla.
 */
public final class MenuStyler {
    private MenuStyler() {}

    private static final Map<ClickableWidget, Float> HOVER = new WeakHashMap<>();
    private static final Map<Screen, Float> FADE = new WeakHashMap<>();
    private static final Map<Screen, Long> OPEN_NS = new WeakHashMap<>();
    private static long lastNs;

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!shouldStyle(screen)) return;
            OPEN_NS.put(screen, System.nanoTime());
            FADE.put(screen, 0f);
            ScreenEvents.afterRender(screen).register((s, ctx, mx, my, tickDelta) -> {
                if (!GlobalSettings.pauseMenu.value && s instanceof GameMenuScreen) return;
                paint(s, ctx, mx, my, tickDelta);
            });
        });
    }

    private static boolean shouldStyle(Screen screen) {
        if (screen == null) return false;
        if (screen instanceof GameMenuScreen) return true;
        if (screen instanceof SelectWorldScreen) return true;
        if (screen instanceof MultiplayerScreen) return true;
        if (screen instanceof OptionsScreen) return true;
        return isCreateWorldRelated(screen);
    }

    private static boolean isCreateWorldRelated(Screen screen) {
        String n = screen.getClass().getName();
        return n.contains("CreateWorld") || n.contains("EditWorld")
                || n.contains("SelectWorld") || n.contains("Multiplayer")
                || n.contains("OptionsScreen") || n.contains("GameMenu");
    }

    private static void paint(Screen screen, DrawContext ctx, int mx, int my, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.textRenderer == null) return;
        TextRenderer tr = mc.textRenderer;

        long now = System.nanoTime();
        float dt = lastNs == 0 ? 0.016f : Math.min(0.05f, (now - lastNs) / 1.0e9f);
        lastNs = now;

        float fade = FADE.getOrDefault(screen, 0f);
        fade += (1f - fade) * (1f - (float) Math.exp(-dt * 9f));
        FADE.put(screen, fade);

        long open = OPEN_NS.getOrDefault(screen, now);
        float t = (now - open) / 1.0e9f;

        // Dim / tint the vanilla background slightly toward purple
        ctx.fill(0, 0, screen.width, screen.height, UI.alpha(0xFF08060F, 0.35f * fade));

        // soft animated orbs behind chrome
        int orbR = (int) (Math.min(screen.width, screen.height) * 0.28f);
        for (int i = 0; i < 3; i++) {
            float ang = t * 0.2f + i * 2.0f;
            int cx = (int) (screen.width * (0.3f + 0.2f * i) + Math.sin(ang) * screen.width * 0.05f);
            int cy = (int) (screen.height * 0.4f + Math.cos(ang * 0.85f) * screen.height * 0.08f);
            UI.disc(ctx, cx, cy, orbR, UI.alpha(UI.ACCENT, 0.035f * fade));
        }

        // header: V logo + title line
        String title = headerTitle(screen);
        int logoSize = 28;
        int logoX = screen.width / 2 - 14;
        int logoY = 12;
        // horizontal accent lines
        int lineW = Math.min(120, screen.width / 5);
        ctx.fill(logoX - lineW - 12, logoY + logoSize / 2, logoX - 10, logoY + logoSize / 2 + 1,
                UI.alpha(UI.ACCENT, fade * 0.85f));
        ctx.fill(logoX + logoSize + 10, logoY + logoSize / 2, logoX + logoSize + 10 + lineW, logoY + logoSize / 2 + 1,
                UI.alpha(UI.ACCENT, fade * 0.85f));
        if (fade > 0.3f) {
            UI.disc(ctx, logoX + logoSize / 2, logoY + logoSize / 2, logoSize / 2 + 6,
                    UI.alpha(UI.ACCENT, 0.15f * fade));
            Logo.draw(ctx, logoX, logoY, logoSize);
        }
        if (title != null && !title.isEmpty()) {
            int tw = tr.getWidth(title);
            ctx.drawText(tr, title, (screen.width - tw) / 2, logoY + logoSize + 6,
                    UI.alpha(UI.TEXT, fade), false);
        }

        // restyle every visible clickable widget as a purple pill/card
        List<ClickableWidget> buttons = collectButtons(screen);
        for (ClickableWidget b : buttons) {
            if (!b.visible) continue;
            int x = b.getX(), y = b.getY(), w = b.getWidth(), h = b.getHeight();
            if (w <= 0 || h <= 0) continue;

            boolean over = b.active && mx >= x && mx < x + w && my >= y && my < y + h;
            float hv = HOVER.getOrDefault(b, 0f);
            hv += ((over ? 1f : 0f) - hv) * (1f - (float) Math.exp(-dt * 16f));
            HOVER.put(b, hv);

            String label = b.getMessage() == null ? "" : b.getMessage().getString();
            boolean danger = label.toLowerCase().contains("delete")
                    || label.toLowerCase().contains("disconnect")
                    || label.toLowerCase().contains("quit");
            boolean primary = label.toLowerCase().contains("play")
                    || label.toLowerCase().contains("join")
                    || label.toLowerCase().contains("create")
                    || label.toLowerCase().contains("done");

            int accent = danger ? 0xFFE0506A : UI.ACCENT;
            int bgBase = primary ? 0xFF1A1433 : UI.SURFACE;
            int bgHover = primary ? 0xFF2A1F55 : UI.SURFACE_HOVER;
            int bg = b.active ? UI.lerp(bgBase, bgHover, hv) : 0xFF110D22;
            int border = b.active ? UI.lerp(UI.BORDER, accent, 0.55f + hv * 0.45f) : 0xFF1C1736;

            // glow on hover
            if (hv > 0.05f && b.active) {
                UI.rrectSoft(ctx, x - 1, y - 1, w + 2, h + 2, UI.alpha(accent, hv * 0.25f * fade));
            }
            UI.card(ctx, x, y, w, h, UI.alpha(bg, fade * 0.95f), UI.alpha(border, fade));

            // left accent bar on hover
            if (hv > 0.05f && b.active) {
                ctx.fill(x + 1, y + 3, x + 3, y + h - 3, UI.alpha(danger ? 0xFFFF8CA0 : UI.ACCENT_LIGHT, hv * fade));
            }

            // label
            int tcol = b.active ? UI.lerp(0xFFD9D4F5, UI.TEXT, hv) : 0xFF5F5885;
            if (label.isEmpty()) label = " ";
            // keep short labels for tiny buttons
            if (w < 36 && label.length() > 1) label = label.substring(0, 1).toUpperCase();
            int tx = x + Math.max(4, (w - tr.getWidth(label)) / 2);
            int ty = y + (h - 8) / 2;
            ctx.drawText(tr, label, tx, ty, UI.alpha(tcol, fade), false);
        }

        // world / server list entries often aren't ClickableWidget buttons —
        // draw a subtle purple frame around the central list area when present
        if (screen instanceof SelectWorldScreen || screen instanceof MultiplayerScreen) {
            int listW = Math.min(400, screen.width - 80);
            int listX = (screen.width - listW) / 2;
            int listY = 70;
            int listH = screen.height - 140;
            // soft outer glow
            UI.rrectSoft(ctx, listX - 2, listY - 2, listW + 4, listH + 4,
                    UI.alpha(UI.ACCENT, 0.08f * fade));
            // thin border
            UI.frame(ctx, listX, listY, listW, listH, UI.alpha(UI.ACCENT, 0.35f * fade));
        }
    }

    private static String headerTitle(Screen screen) {
        if (screen instanceof SelectWorldScreen) return "Select World";
        if (screen instanceof MultiplayerScreen) return "Play Multiplayer";
        if (screen instanceof OptionsScreen) return "Options";
        if (screen instanceof GameMenuScreen) return "Game Menu";
        String n = screen.getClass().getSimpleName();
        if (n.contains("Create")) return "Create New World";
        if (n.contains("Edit")) return "Edit World";
        return "";
    }

    /** Best-effort collection of clickable widgets from a screen. */
    @SuppressWarnings("unchecked")
    private static List<ClickableWidget> collectButtons(Screen screen) {
        List<ClickableWidget> out = new ArrayList<>();
        try {
            // Screen.children() exists on modern versions
            for (var child : screen.children()) {
                if (child instanceof ClickableWidget cw) out.add(cw);
            }
        } catch (Throwable ignored) {
            // fallback: reflect common field names
            try {
                for (var field : screen.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    Object v = field.get(screen);
                    if (v instanceof ClickableWidget cw) out.add(cw);
                    else if (v instanceof List<?> list) {
                        for (Object o : list) if (o instanceof ClickableWidget cw) out.add(cw);
                    }
                }
            } catch (Throwable ignored2) {}
        }
        return out;
    }
}
