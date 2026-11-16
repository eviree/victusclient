package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;

/** Switches to third person on elytra / in vehicles and back again afterwards. */
public class AutoPerspectiveModule extends Module {
    private final Setting.Bool elytra = add(new Setting.Bool("Elytra", true));
    private final Setting.Bool vehicles = add(new Setting.Bool("Vehicles", true));
    private Perspective saved;

    public AutoPerspectiveModule() { super("Auto Perspective", Category.VISUAL); }

    @Override public void onTick(MinecraftClient mc) {
        if (mc.player == null) return;
        boolean want = (elytra.value && mc.player.isFallFlying()) || (vehicles.value && mc.player.hasVehicle());
        if (want && saved == null) {
            saved = mc.options.getPerspective();
            mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        } else if (!want && saved != null) {
            mc.options.setPerspective(saved);
            saved = null;
        }
    }

    @Override public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (saved != null && mc.options != null) mc.options.setPerspective(saved);
        saved = null;
    }
}
