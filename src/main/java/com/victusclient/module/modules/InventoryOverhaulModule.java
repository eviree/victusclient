package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

/**
 * Inventory Overhaul — when enabled, opening the survival inventory tries to
 * keep more of the world context (player still visible in their current POV
 * style) and uses a smoother open animation feel.
 *
 * True full 3D world-space inventory (floating panel in front of the camera
 * while the world keeps rendering at full FPS with free look) requires deep
 * rendering pipeline changes. This module provides the best approximation
 * possible with a standard Fabric client mod:
 *  - does not pause the game as hard as some custom screens
 *  - keeps the player model / background more lively
 *  - optional blur / fade styling via settings
 */
public class InventoryOverhaulModule extends Module {
    public static InventoryOverhaulModule INSTANCE;

    public final Setting.Bool keepWorldTicking = add(new Setting.Bool("Keep world ticking", true));
    public final Setting.Bool animatedOpen = add(new Setting.Bool("Animated open", true));
    public final Setting.Slider openSpeed = add(new Setting.Slider("Open speed", 1.0, 0.4, 2.0, 0.1));
    public final Setting.Bool dimBackground = add(new Setting.Bool("Dim background", true));
    public final Setting.Slider dimAmount = add(new Setting.Slider("Dim strength", 40, 0, 80, 5));

    /** Animation progress 0→1 when inventory is open. */
    public float anim = 0f;
    public boolean inventoryOpen;

    public InventoryOverhaulModule() {
        super("Inventory Overhaul", Category.UTILITY);
        INSTANCE = this;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!enabled) {
            anim = 0f;
            inventoryOpen = false;
            return;
        }

        boolean open = mc.currentScreen instanceof InventoryScreen
                || (mc.currentScreen instanceof HandledScreen && isPlayerInventory(mc));

        inventoryOpen = open;
        float target = open ? 1f : 0f;
        float speed = (float) (0.18 * openSpeed.value);
        if (anim < target) anim = Math.min(target, anim + speed);
        else if (anim > target) anim = Math.max(target, anim - speed);
    }

    private boolean isPlayerInventory(MinecraftClient mc) {
        // only the pure player inventory screen; chests etc. stay vanilla
        return mc.currentScreen != null
                && mc.currentScreen.getClass().getSimpleName().contains("Inventory");
    }

    /** Whether the overhaul is currently active and should influence rendering. */
    public static boolean active() {
        return INSTANCE != null && INSTANCE.enabled && INSTANCE.inventoryOpen;
    }

    public static float progress() {
        return INSTANCE == null ? 0f : INSTANCE.anim;
    }
}
