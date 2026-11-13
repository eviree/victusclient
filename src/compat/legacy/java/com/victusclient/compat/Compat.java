package com.victusclient.compat;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import java.util.function.Consumer;

public final class Compat {
    private Compat() {}

    public static KeyBinding registerMenuKey() {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.victusclient.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.victusclient"));
    }

    public static void registerHud(Consumer<DrawContext> renderer) {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> renderer.accept(ctx));
    }
}
