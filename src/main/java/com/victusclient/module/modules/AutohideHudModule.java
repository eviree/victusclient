package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.module.ModuleManager;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;

/** Hides the Victus HUD while you stand still, shows it again when you move or look around. */
public class AutohideHudModule extends Module {
    private final Setting.Slider delay = add(new Setting.Slider("Hide After (s)", 8, 2, 60, 1));
    private double lx, ly, lz;
    private float lyaw, lpitch;
    private int lslot = -1;
    private long lastActive = System.currentTimeMillis();

    public AutohideHudModule() { super("Autohide HUD", Category.UTILITY); }

    @Override public void onTick(MinecraftClient mc) {
        if (mc.player == null) return;
        boolean active = mc.currentScreen != null
                || Math.abs(mc.player.getX() - lx) > 0.01 || Math.abs(mc.player.getY() - ly) > 0.01
                || Math.abs(mc.player.getZ() - lz) > 0.01
                || Math.abs(mc.player.getYaw() - lyaw) > 0.5f || Math.abs(mc.player.getPitch() - lpitch) > 0.5f
                || mc.player.hurtTime > 0;
        lx = mc.player.getX(); ly = mc.player.getY(); lz = mc.player.getZ();
        lyaw = mc.player.getYaw(); lpitch = mc.player.getPitch();
        if (active) lastActive = System.currentTimeMillis();
        ModuleManager.hudHidden = System.currentTimeMillis() - lastActive > delay.value * 1000;
    }

    @Override public void onDisable() { ModuleManager.hudHidden = false; }
}
