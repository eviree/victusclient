package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

/**
 * Forces entity hitboxes to render (same idea as F3+B) while enabled.
 * Uses the debug hitbox flag so it works across versions without custom line rendering.
 */
public class HitboxModule extends Module {
    private final Setting.Bool playersOnly = add(new Setting.Bool("Players only", false));
    private final Setting.Slider range = add(new Setting.Slider("Range", 64, 16, 128, 8));

    private boolean prevDebug;

    public HitboxModule() {
        super("Hitbox", Category.VISUAL);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        prevDebug = mc.getEntityRenderDispatcher() != null
                && mc.getEntityRenderDispatcher().shouldRenderHitboxes();
        if (mc.getEntityRenderDispatcher() != null) {
            mc.getEntityRenderDispatcher().setRenderHitboxes(true);
        }
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.getEntityRenderDispatcher() != null) {
            mc.getEntityRenderDispatcher().setRenderHitboxes(prevDebug);
        }
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.getEntityRenderDispatcher() != null
                && !mc.getEntityRenderDispatcher().shouldRenderHitboxes()) {
            mc.getEntityRenderDispatcher().setRenderHitboxes(true);
        }
    }
}
