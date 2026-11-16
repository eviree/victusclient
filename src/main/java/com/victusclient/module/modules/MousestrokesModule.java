package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public class MousestrokesModule extends HudModule {
    public MousestrokesModule() { super("Mousestrokes", 6, 470); w = 62; h = 22; }

    private void btn(DrawContext ctx, MinecraftClient mc, int bx, String label, boolean down) {
        ctx.fill(bx, y, bx + 30, y + 22, down ? 0xA0FFFFFF : bgColor());
        ctx.drawText(mc.textRenderer, label, bx + 15 - mc.textRenderer.getWidth(label) / 2, y + 7,
                down ? 0xFF000000 : textColor(), shadow.value && !down);
    }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        w = 62; h = 22;
        long hnd = mc.getWindow().getHandle();
        btn(ctx, mc, x, "LMB", GLFW.glfwGetMouseButton(hnd, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS);
        btn(ctx, mc, x + 32, "RMB", GLFW.glfwGetMouseButton(hnd, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS);
    }
}
