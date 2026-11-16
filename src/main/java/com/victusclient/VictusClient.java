package com.victusclient;

import com.victusclient.compat.Compat;
import com.victusclient.config.Config;
import com.victusclient.gui.ClickGUIScreen;
import com.victusclient.gui.MenuStyler;
import com.victusclient.gui.VictusTitleScreen;
import com.victusclient.module.ModuleManager;
import com.victusclient.module.modules.DropPreventionModule;
import com.victusclient.module.modules.StopwatchModule;
import com.victusclient.module.modules.ZoomModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class VictusClient implements ClientModInitializer {
    public static final String ID = "victusclient";
    public static final int ACCENT = 0xFF8B6CFF; // Victus violet
    public static KeyBinding menuKey;

    public static String version() {
        return FabricLoader.getInstance().getModContainer(ID)
                .map(m -> m.getMetadata().getVersion().getFriendlyString()).orElse("dev");
    }

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        Config.load();

        menuKey = Compat.registerKey("key.victusclient.menu", GLFW.GLFW_KEY_RIGHT_SHIFT);
        ZoomModule.KEY = Compat.registerKey("key.victusclient.zoom", GLFW.GLFW_KEY_C);
        StopwatchModule.TOGGLE = Compat.registerKey("key.victusclient.stopwatch", GLFW.GLFW_KEY_UNKNOWN);
        StopwatchModule.RESET = Compat.registerKey("key.victusclient.stopwatch_reset", GLFW.GLFW_KEY_UNKNOWN);

        MenuStyler.init(); // Victus-styled Esc menu

        // before vanilla reads the keys (Drop Prevention)
        ClientTickEvents.START_CLIENT_TICK.register(DropPreventionModule::startTick);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (menuKey.wasPressed()) {
                if (mc.currentScreen == null) mc.setScreen(new ClickGUIScreen());
            }
            ModuleManager.tick(mc);
        });

        // Victus title screen replaces the vanilla one (switch off in Menu > Settings)
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (GlobalSettings.titleScreen.value && screen.getClass() == TitleScreen.class) {
                client.send(() -> {
                    if (client.currentScreen == screen) client.setScreen(new VictusTitleScreen());
                });
            }
        });

        Compat.registerHud(ctx -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden) return;
            ModuleManager.render(ctx, mc);
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> {
            ZoomModule.restoreSensitivity();
            Config.save();
        });
    }
}
