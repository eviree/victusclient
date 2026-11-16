package com.victusclient.gui;

import com.victusclient.Logo;
import com.victusclient.VictusClient;
import com.victusclient.compat.VScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Victus title screen matching the modern purple design:
 * large V logo top-left, left-aligned text buttons, account chip top-right,
 * animated background, staggered fade/slide-in.
 */
public class VictusTitleScreen extends VScreen {
    private static class Btn {
        final String label;
        final Runnable action;
        float hover;
        float appear; // 0→1 entrance
        int x, y, w, h;
        Btn(String label, Runnable action) { this.label = label; this.action = action; }
        boolean in(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    private final List<Btn> btns = new ArrayList<>();
    private final long openNs = System.nanoTime();
    private long lastNs;
    private float logoAppear;
    private int gearX, gearY, gearS = 18;
    private int accountX, accountY, accountW;

    public VictusTitleScreen() { super(Text.literal("Victus Client")); }

    @Override public boolean shouldCloseOnEsc() { return false; }
    @Override public boolean shouldPause() { return false; }

    @Override
    protected void init() {
        btns.clear();
        // Left column layout like the reference
        int left = 48;
        int top = height / 2 - 20;
        int gap = 28;

        btns.add(new Btn("SINGLEPLAYER", () -> client.setScreen(new SelectWorldScreen(this))));
        btns.add(new Btn("MULTIPLAYER", () -> client.setScreen(new MultiplayerScreen(this))));
        btns.add(new Btn("QUIT GAME", () -> client.scheduleStop()));

        for (int i = 0; i < btns.size(); i++) {
            Btn b = btns.get(i);
            b.w = 160;
            b.h = 18;
            b.x = left;
            b.y = top + i * gap;
            b.appear = 0f;
            b.hover = 0f;
        }

        // top-right account + icons
        String name = client != null && client.getSession() != null
                ? client.getSession().getUsername() : "Player";
        accountW = textRenderer.getWidth(name) + 28;
        accountX = width - accountW - 52;
        accountY = 14;
        gearX = width - 28;
        gearY = 14;
        logoAppear = 0f;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        long now = System.nanoTime();
        float dt = lastNs == 0 ? 0.016f : Math.min(0.05f, (now - lastNs) / 1.0e9f);
        lastNs = now;
        float t = (now - openNs) / 1.0e9f;

        // --- animated dark purple atmosphere ---
        UI.vgrad(ctx, 0, 0, width, height, 0xFF06040F, 0xFF120C28);

        // drifting soft orbs
        int orbR = (int) (Math.min(width, height) * 0.34f);
        for (int i = 0; i < 4; i++) {
            float ang = t * 0.18f + i * 1.7f;
            int cx = (int) (width * (0.25f + 0.18f * i) + Math.sin(ang) * width * 0.06f);
            int cy = (int) (height * (0.35f + 0.1f * (i % 3)) + Math.cos(ang * 0.9f) * height * 0.08f);
            UI.disc(ctx, cx, cy, orbR, UI.alpha(UI.ACCENT, 0.04f));
            UI.disc(ctx, cx, cy, (int) (orbR * 0.55f), UI.alpha(UI.ACCENT, 0.05f));
        }

        // rising particles
        for (int i = 0; i < 48; i++) {
            float speed = 5f + (i * 41 % 12);
            float fx = (i * 97 % 1000) / 1000f;
            float fy = (i * 53 % 1000) / 1000f;
            int x = (int) (fx * width + Math.sin(t * 0.55f + i) * 10f);
            int y = (int) (height - ((t * speed + fy * height) % (height + 20)));
            float tw = 0.25f + 0.4f * (float) Math.sin(t * 2.2f + i);
            int sz = (i % 4 == 0) ? 2 : 1;
            ctx.fill(x, y, x + sz, y + sz, UI.alpha(UI.ACCENT_LIGHT, tw));
        }

        // vignette
        UI.vgrad(ctx, 0, 0, width, height / 5, 0x9903020A, 0x0003020A);
        UI.vgrad(ctx, 0, height * 4 / 5, width, height / 5, 0x0003020A, 0xAA03020A);

        // --- logo top-left (fade + subtle bob) ---
        logoAppear += (1f - logoAppear) * (1f - (float) Math.exp(-dt * 6f));
        int logoSize = 56;
        int logoX = 36;
        int logoY = 28 + (int) (Math.sin(t * 1.4f) * 2f);
        if (logoAppear > 0.05f) {
            // soft glow under logo
            UI.disc(ctx, logoX + logoSize / 2, logoY + logoSize / 2, logoSize / 2 + 10,
                    UI.alpha(UI.ACCENT, 0.12f * logoAppear));
            Logo.draw(ctx, logoX, logoY, logoSize);
        }

        // --- top-right account chip + gear ---
        float topFade = Math.min(1f, t * 2.5f);
        String name = client != null && client.getSession() != null
                ? client.getSession().getUsername() : "Player";
        // account background
        UI.card(ctx, accountX, accountY, accountW, 20,
                UI.alpha(0xFF141022, topFade * 0.85f), UI.alpha(UI.BORDER, topFade * 0.7f));
        // mini face placeholder
        ctx.fill(accountX + 4, accountY + 4, accountX + 16, accountY + 16, UI.alpha(0xFF3A2D78, topFade));
        ctx.drawText(textRenderer, name, accountX + 20, accountY + 6, UI.alpha(UI.TEXT, topFade), false);

        // V icon button
        int vx = width - 48;
        UI.card(ctx, vx, accountY, 18, 18, UI.alpha(0xFF141022, topFade * 0.85f), UI.alpha(UI.BORDER, topFade * 0.7f));
        ctx.drawText(textRenderer, "V", vx + 5, accountY + 5, UI.alpha(UI.ACCENT_LIGHT, topFade), false);

        // gear (opens options)
        boolean gearHover = mx >= gearX && mx < gearX + gearS && my >= gearY && my < gearY + gearS;
        UI.card(ctx, gearX, gearY, gearS, gearS,
                UI.alpha(gearHover ? UI.SURFACE_HOVER : 0xFF141022, topFade * 0.9f),
                UI.alpha(gearHover ? UI.ACCENT : UI.BORDER, topFade));
        ctx.drawText(textRenderer, "⚙", gearX + 4, gearY + 4, UI.alpha(UI.MUTED, topFade), false);

        // --- left menu buttons (staggered slide + hover underline) ---
        for (int i = 0; i < btns.size(); i++) {
            Btn b = btns.get(i);
            float target = t > 0.15f + i * 0.08f ? 1f : 0f;
            b.appear += (target - b.appear) * (1f - (float) Math.exp(-dt * 10f));
            boolean over = b.in(mx, my);
            b.hover += ((over ? 1f : 0f) - b.hover) * (1f - (float) Math.exp(-dt * 14f));

            int slide = (int) ((1f - b.appear) * 24);
            int drawX = b.x - slide;
            int alpha = (int) (b.appear * 255);
            if (alpha <= 0) continue;

            int col = UI.lerp(UI.MUTED, UI.TEXT, b.hover);
            col = UI.alpha(col, b.appear);
            ctx.drawText(textRenderer, b.label, drawX, b.y, col, false);

            // animated underline
            if (b.hover > 0.02f) {
                int uw = (int) (textRenderer.getWidth(b.label) * b.hover);
                ctx.fill(drawX, b.y + 12, drawX + uw, b.y + 13, UI.alpha(UI.ACCENT, b.hover * b.appear));
            }
        }

        // footer
        float foot = Math.min(1f, t * 1.5f);
        ctx.drawText(textRenderer, "Victus Client", 16, height - 18, UI.alpha(UI.MUTED2, foot * 0.8f), false);
        String ver = "v" + VictusClient.version();
        ctx.drawText(textRenderer, ver, width - textRenderer.getWidth(ver) - 12, height - 18,
                UI.alpha(UI.MUTED2, foot * 0.8f), false);
    }

    @Override
    protected boolean onClick(double mx, double my, int button) {
        if (button != 0) return false;
        for (Btn b : btns) {
            if (b.in(mx, my)) { b.action.run(); return true; }
        }
        // gear → options
        if (mx >= gearX && mx < gearX + gearS && my >= gearY && my < gearY + gearS) {
            client.setScreen(new OptionsScreen(this, client.options));
            return true;
        }
        return false;
    }

    @Override
    protected boolean onKey(int key) {
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            if (!btns.isEmpty()) btns.get(0).action.run();
            return true;
        }
        return false;
    }
}
