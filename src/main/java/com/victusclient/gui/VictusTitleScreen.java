package com.victusclient.gui;

import com.victusclient.Logo;
import com.victusclient.VictusClient;
import com.victusclient.compat.VScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.ArrayList;
import java.util.List;

/** Victus main menu: animated violet background, floating logo, animated buttons. */
public class VictusTitleScreen extends VScreen {
    private static class Btn {
        final String label; final Runnable action;
        float hover; int x, y, w, h;
        Btn(String label, Runnable action) { this.label = label; this.action = action; }
        boolean in(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    private final List<Btn> btns = new ArrayList<>();
    private final long openNs = System.nanoTime();
    private long lastNs;
    private int logoSize, logoY, titleY, tagY;

    public VictusTitleScreen() { super(Text.literal("Victus Client")); }

    @Override public boolean shouldCloseOnEsc() { return false; }
    @Override public boolean shouldPause() { return false; }

    @Override
    protected void init() {
        btns.clear();
        int bw = 180, bh = 22, gap = 6;
        logoSize = Math.max(48, Math.min(96, height / 4));
        int total = logoSize + 8 + 12 + 12 + 14 + 4 * (bh + gap) - gap;
        logoY = Math.max(6, (height - total) / 2);
        titleY = logoY + logoSize + 8;
        tagY = titleY + 12;
        int top = tagY + 26;

        btns.add(new Btn("Singleplayer", () -> client.setScreen(new SelectWorldScreen(this))));
        btns.add(new Btn("Multiplayer", () -> client.setScreen(new MultiplayerScreen(this))));
        btns.add(new Btn("Options", () -> client.setScreen(new OptionsScreen(this, client.options))));
        btns.add(new Btn("Quit Game", () -> client.scheduleStop()));
        for (int i = 0; i < btns.size(); i++) {
            Btn b = btns.get(i);
            b.w = bw; b.h = bh; b.x = (width - bw) / 2; b.y = top + i * (bh + gap);
        }
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        long now = System.nanoTime();
        float dt = lastNs == 0 ? 0.016f : Math.min(0.05f, (now - lastNs) / 1.0e9f);
        lastNs = now;
        float t = (now - openNs) / 1.0e9f;

        // background gradient
        UI.vgrad(ctx, 0, 0, width, height, 0xFF09061A, 0xFF150E33);
        // drifting violet orbs
        int orbR = (int) (Math.min(width, height) * 0.30f);
        for (int i = 0; i < 3; i++) {
            float ang = t * 0.22f + i * 2.1f;
            int cx = (int) (width * (0.2f + 0.3f * i) + Math.sin(ang) * width * 0.07f);
            int cy = (int) (height * (0.40f + 0.12f * i) + Math.cos(ang * 0.8f) * height * 0.10f);
            UI.disc(ctx, cx, cy, orbR, UI.alpha(UI.ACCENT, 0.045f));
            UI.disc(ctx, cx, cy, (int) (orbR * 0.62f), UI.alpha(UI.ACCENT, 0.05f));
        }
        // rising particles
        for (int i = 0; i < 40; i++) {
            float speed = 6f + (i * 37 % 10);
            float fx = (i * 97 % 1000) / 1000f;
            float fy = (i * 53 % 1000) / 1000f;
            int x = (int) (fx * width + Math.sin(t * 0.6f + i) * 8f);
            int y = (int) (height - ((t * speed + fy * height) % height));
            float tw = 0.35f + 0.35f * (float) Math.sin(t * 2f + i);
            int sz = (i % 3 == 0) ? 2 : 1;
            ctx.fill(x, y, x + sz, y + sz, UI.alpha(UI.ACCENT_LIGHT, tw));
        }
        UI.vgrad(ctx, 0, 0, width, height / 4, 0xAA05030D, 0x0005030D);
        UI.vgrad(ctx, 0, height * 3 / 4, width, height - height * 3 / 4, 0x0005030D, 0xAA05030D);

        // logo + title
        float a = Math.min(1f, t * 2.2f);
        int bob = (int) (Math.sin(t * 1.6f) * 2.5f);
        if (a > 0.45f) Logo.draw(ctx, (width - logoSize) / 2, logoY + bob, logoSize);
        if (a > 0.08f) {
            Text title = Text.literal("VICTUS CLIENT").formatted(Formatting.BOLD);
            ctx.drawText(textRenderer, title, (width - textRenderer.getWidth(title)) / 2, titleY, UI.alpha(UI.TEXT, a), false);
            String tag = "MORE THAN JUST A CLIENT";
            ctx.drawText(textRenderer, tag, (width - textRenderer.getWidth(tag)) / 2, tagY, UI.alpha(UI.ACCENT_LIGHT, a), false);
        }

        // buttons (staggered slide-in, hover glow)
        for (int i = 0; i < btns.size(); i++) {
            Btn b = btns.get(i);
            float p = Math.max(0f, Math.min(1f, (t - 0.25f - i * 0.08f) * 4f));
            if (p < 0.06f) continue;
            float e = 1f - (1f - p) * (1f - p) * (1f - p);
            int bx = b.x + (int) ((1f - e) * 26);
            boolean over = p > 0.9f && b.in(mx, my);
            b.hover += ((over ? 1f : 0f) - b.hover) * (1f - (float) Math.exp(-dt * 16f));

            UI.card(ctx, bx, b.y, b.w, b.h,
                    UI.alpha(UI.lerp(UI.SURFACE, UI.SURFACE_HOVER, b.hover), e),
                    UI.alpha(UI.lerp(UI.BORDER, UI.ACCENT, b.hover), e));
            if (b.hover > 0.05f) ctx.fill(bx + 1, b.y + 3, bx + 3, b.y + b.h - 3, UI.alpha(UI.ACCENT_LIGHT, b.hover * e));
            int tx = bx + (b.w - textRenderer.getWidth(b.label)) / 2 + (int) (b.hover * 2);
            ctx.drawText(textRenderer, b.label, tx, b.y + 7, UI.alpha(UI.lerp(UI.MUTED, UI.TEXT, b.hover), e), false);
        }

        ctx.drawText(textRenderer, "Victus Client v" + VictusClient.version(), 8, height - 14, UI.alpha(UI.MUTED, 0.8f), false);
        String hint = "RShift in-game: Victus Menu";
        ctx.drawText(textRenderer, hint, width - 8 - textRenderer.getWidth(hint), height - 14, UI.alpha(UI.MUTED, 0.8f), false);
    }

    @Override
    protected boolean onClick(double mx, double my, int button) {
        if (button != 0) return false;
        for (Btn b : btns) {
            if (b.in(mx, my)) { b.action.run(); return true; }
        }
        return false;
    }
}
