package com.victusclient.gui;

import com.victusclient.config.Config;
import com.victusclient.module.HudModule;
import com.victusclient.module.Module;
import com.victusclient.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import com.victusclient.compat.VScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class HudEditorScreen extends VScreen {
    private final Screen parent;
    private HudModule dragged;
    private int offX, offY;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override public boolean shouldPause() { return false; }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fill(0, 0, width, height, 0x80000000);
        String t = "HUD EDITOR";
        ctx.drawText(textRenderer, Text.literal(t).formatted(Formatting.BOLD), width / 2 - textRenderer.getWidth(t) / 2, 12, UI.ACCENT, false);
        String sub = "Drag elements anywhere  -  RShift / Esc to finish";
        ctx.drawText(textRenderer, sub, width / 2 - textRenderer.getWidth(sub) / 2, 26, UI.MUTED, false);
        for (Module m : ModuleManager.all()) {
            if (m instanceof HudModule h && h.enabled) {
                UI.frame(ctx, h.x - 2, h.y - 2, h.w + 4, h.h + 4, h == dragged ? UI.ACCENT : 0xFF8A8A96);
            }
        }
    }

    @Override
    protected boolean onClick(double mx, double my, int button) {
        if (button == 0) {
            for (Module m : ModuleManager.all()) {
                if (m instanceof HudModule h && h.enabled
                        && mx >= h.x && mx <= h.x + h.w && my >= h.y && my <= h.y + h.h) {
                    dragged = h; offX = (int) mx - h.x; offY = (int) my - h.y;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected boolean onDrag(double mx, double my, int button) {
        if (dragged != null) {
            dragged.x = Math.max(0, Math.min(width - dragged.w, (int) mx - offX));
            dragged.y = Math.max(0, Math.min(height - dragged.h, (int) my - offY));
            return true;
        }
        return false;
    }

    @Override
    protected boolean onRelease(double mx, double my, int button) {
        dragged = null;
        return false;
    }

    @Override
    protected boolean onKey(int key) {
        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) { close(); return true; }
        return false;
    }

    @Override public void close() { client.setScreen(parent); }
    @Override public void removed() { Config.save(); }
}
