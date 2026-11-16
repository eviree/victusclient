package com.victusclient.gui;

import com.victusclient.GlobalSettings;
import com.victusclient.Logo;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Restyles the vanilla Game Menu (Esc) in the Victus violet theme.
 * The vanilla buttons stay in place and keep working (clicks, keyboard, disabled state);
 * they are made almost invisible and Victus-styled buttons are drawn on top of them.
 */
public final class MenuStyler {
    private MenuStyler() {}

    private static final Map<ClickableWidget, Float> HOVER = new IdentityHashMap<>();

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (GlobalSettings.pauseMenu.value && screen instanceof GameMenuScreen) style(screen);
        });
    }

    private static void style(Screen screen) {
        final List<ClickableWidget> buttons = Screens.getButtons(screen);
        for (ClickableWidget b : buttons) b.setAlpha(0.02f); // not 0: vanilla would draw text opaque at alpha 0
        final long open = System.nanoTime();
        final long[] last = {0L};
        HOVER.clear();
        ScreenEvents.afterRender(screen).register((s, ctx, mx, my, tickDelta) -> {
            long now = System.nanoTime();
            float dt = last[0] == 0L ? 0.016f : Math.min(0.05f, (now - last[0]) / 1.0e9f);
            last[0] = now;
            float fade = GlobalSettings.animations.value ? Math.min(1f, (now - open) / 1.8e8f) : 1f;
            draw(s, ctx, mx, my, buttons, fade, dt);
        });
    }

    private static void draw(Screen s, DrawContext ctx, int mx, int my, List<ClickableWidget> buttons, float fade, float dt) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = 0, maxY = 0;
        for (ClickableWidget b : buttons) {
            if (!b.visible) continue;
            minX = Math.min(minX, b.getX());
            minY = Math.min(minY, b.getY());
            maxX = Math.max(maxX, b.getX() + b.getWidth());
            maxY = Math.max(maxY, b.getY() + b.getHeight());
        }
        if (minX == Integer.MAX_VALUE) return;

        // dim + violet tint over the blurred world
        ctx.fill(0, 0, s.width, s.height, UI.alpha(0xA005030D, fade));
        UI.vgrad(ctx, 0, s.height / 2, s.width, s.height - s.height / 2, UI.alpha(0x00000000, fade), UI.alpha(0x553B2A8C, fade));

        int pad = 14, top = minY - 56;
        int px = minX - pad, pw = (maxX - minX) + pad * 2;
        int py = top - (int) ((1f - fade) * 12), ph = (maxY + pad) - top;

        for (int i = 3; i >= 1; i--) UI.rrect(ctx, px - i, py - i, pw + 2 * i, ph + 2 * i, UI.alpha(UI.ACCENT, fade * 0.10f * (4 - i)));
        UI.rrect(ctx, px, py, pw, ph, UI.alpha(0xFF3A2D78, fade));
        UI.vgrad(ctx, px + 1, py + 2, pw - 2, ph - 4,
                UI.alpha(UI.PANEL_TOP, fade * 0.96f), UI.alpha(UI.PANEL_BOTTOM, fade * 0.96f));

        // header: logo + title
        if (fade > 0.5f) Logo.draw(ctx, px + 12, py + 9, 30);
        Text vic = Text.literal("VICTUS").formatted(Formatting.BOLD);
        ctx.drawText(tr, vic, px + 48, py + 12, UI.alpha(UI.TEXT, fade), false);
        ctx.drawText(tr, Text.literal("CLIENT").formatted(Formatting.BOLD), px + 48 + tr.getWidth(vic) + 4, py + 12,
                UI.alpha(UI.ACCENT_LIGHT, fade), false);
        ctx.drawText(tr, "GAME MENU", px + 48, py + 24, UI.alpha(UI.MUTED, fade), false);
        ctx.fill(px + 12, py + 44, px + pw - 12, py + 45, UI.alpha(UI.BORDER, fade));

        // restyled buttons
        for (ClickableWidget b : buttons) {
            if (!b.visible) continue;
            int x = b.getX(), y = b.getY(), w = b.getWidth(), h = b.getHeight();
            boolean over = b.active && mx >= x && mx < x + w && my >= y && my < y + h;
            float hv = HOVER.getOrDefault(b, 0f);
            hv += ((over ? 1f : 0f) - hv) * (1f - (float) Math.exp(-dt * 16f));
            HOVER.put(b, hv);

            String label = b.getMessage().getString();
            boolean danger = label.toLowerCase().contains("disconnect") || label.toLowerCase().contains("quit");
            int accent = danger ? 0xFFE0506A : UI.ACCENT;

            int bg = b.active ? UI.lerp(UI.SURFACE, danger ? 0xFF3A1A2E : UI.SURFACE_HOVER, hv) : 0xFF110D22;
            int border = b.active ? UI.lerp(UI.BORDER, accent, hv) : 0xFF1C1736;
            UI.card(ctx, x, y, w, h, UI.alpha(bg, fade), UI.alpha(border, fade));
            if (hv > 0.05f) ctx.fill(x + 1, y + 3, x + 3, y + h - 3, UI.alpha(danger ? 0xFFFF8CA0 : UI.ACCENT_LIGHT, hv * fade));

            int tcol = b.active ? UI.lerp(0xFFD9D4F5, UI.TEXT, hv) : 0xFF5F5885;
            if (label.isEmpty()) label = "?";
            if (w < 40) label = label.substring(0, 1).toUpperCase(); // icon-only buttons
            int tx = x + (w - tr.getWidth(label)) / 2;
            ctx.drawText(tr, label, tx, y + (h - 8) / 2, UI.alpha(tcol, fade), false);
        }
    }
}
