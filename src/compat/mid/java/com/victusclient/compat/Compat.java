package com.victusclient.compat;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import java.util.function.Consumer;

public final class Compat {
    private Compat() {}

    public static KeyBinding registerKey(String translationKey, int defaultKey) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                translationKey, InputUtil.Type.KEYSYM, defaultKey, "category.victusclient"));
    }

    public static void registerHud(Consumer<DrawContext> renderer) {
        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT, Identifier.of("victusclient", "hud"),
                (ctx, tickCounter) -> renderer.accept(ctx));
    }
}
