package com.victusclient.module;

import com.victusclient.module.modules.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    public static void init() {
        // HUD
        register(new FpsModule());
        register(new CpsModule());
        register(new CoordsModule());
        register(new KeystrokesModule());
        register(new PingModule());
        register(new SpeedModule());
        register(new DirectionModule());
        register(new ClockModule());
        register(new SessionTimerModule());
        register(new MemoryModule());
        register(new WorldInfoModule());
        register(new ServerInfoModule());
        register(new HungerModule());
        // PvP
        register(new ArmorStatusModule());
        register(new PotionStatusModule());
        register(new ComboModule());
        register(new ReachModule());
        register(new ItemCounterModule());
        // Render
        register(new FullbrightModule());
        register(new ZoomModule());
        // Movement
        register(new ToggleSprintModule());

        get("FPS").setEnabled(true);
        get("CPS").setEnabled(true);
    }

    public static void register(Module m) { MODULES.add(m); }
    public static List<Module> all() { return MODULES; }

    public static Module get(String name) {
        for (Module m : MODULES) if (m.name.equalsIgnoreCase(name)) return m;
        return null;
    }

    public static void tick(MinecraftClient mc) {
        for (Module m : MODULES) if (m.enabled) m.onTick(mc);
    }

    public static void render(DrawContext ctx, MinecraftClient mc) {
        for (Module m : MODULES) if (m.enabled) m.onRender(ctx, mc);
    }
}
