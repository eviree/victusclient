package com.victusclient.module;

import com.victusclient.module.modules.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();
    public static boolean hudHidden; // set by Autohide HUD

    private static void reg(Module m, String desc) { m.description = desc; MODULES.add(m); }
    private static void soon(String name, Category c, String desc) { MODULES.add(new ComingSoonModule(name, c, desc)); }

    public static void init() {
        // ---------- Victus special ----------
        reg(new MomentumModule(), "Build momentum with every hit.");

        // ---------- PvP ----------
        reg(new ArmorStatusModule(), "Armor pieces with durability.");
        reg(new ComboModule(), "Counts your consecutive hits.");
        reg(new ReachModule(), "Distance of your last hit.");
        reg(new PotionStatusModule(), "Active effects with timers.");
        reg(new ItemCounterModule(), "Pearls, totems, gapples & more.");
        reg(new AttackIndicatorModule(), "Cooldown bar under crosshair.");
        reg(new HitIndicatorModule(), "Marker when you land a hit.");
        reg(new DamageIndicatorModule(), "Red flash when you take damage.");
        reg(new TotemModule(), "Totems of Undying you carry.");

        // ---------- HUD ----------
        reg(new FpsModule(), "Shows your frame rate.");
        reg(new CpsModule(), "Clicks per second.");
        reg(new PingModule(), "Latency to the server.");
        reg(new CoordsModule(), "Your X, Y, Z position.");
        reg(new DirectionModule(), "Direction you are facing.");
        reg(new ClockModule(), "Real-world clock.");
        reg(new SpeedModule(), "Your speed in blocks/sec.");
        reg(new SessionTimerModule(), "Time played this session.");
        reg(new MemoryModule(), "RAM usage and CPU cores.");
        reg(new ServerInfoModule(), "Server IP and player count.");
        reg(new HungerModule(), "Food and saturation levels.");
        reg(new KeystrokesModule(), "WASD key display.");
        reg(new MousestrokesModule(), "Mouse button display.");
        reg(new StopwatchModule(), "Start, pause and reset timer.");
        reg(new ItemInfoModule(), "Details about the held item.");
        reg(new InventoryModule(), "Your inventory on the HUD.");
        reg(new PackDisplayModule(), "Active resource packs.");
        reg(new DeathInfoModule(), "Where you died last.");
        reg(new BlockIndicatorModule(), "Block you are looking at.");
        reg(new MinimapModule(), "Live minimap of the area around you.");
        reg(new TpsModule(), "Approximate server tick rate.");

        // ---------- Visual ----------
        reg(new FullbrightModule(), "Full brightness, no more dark.");
        reg(new ZoomModule(), "Smooth zoom. Hold C, scroll to adjust.");
        reg(new FovChangerModule(), "FOV beyond the vanilla limit.");
        reg(new AutoPerspectiveModule(), "Third person on elytra & rides.");
        reg(new CrosshairModule(), "Custom crosshair styles and colors.");
        reg(new HitboxModule(), "Show entity hitboxes.");
        reg(new FreelookModule(), "Hold Left Alt for third-person look.");

        // ---------- Movement ----------
        reg(new ToggleSprintModule(), "Sprint without holding the key.");
        reg(new ElytraModule(), "Elytra durability HUD and low-durability warn.");

        // ---------- World ----------
        reg(new WorldInfoModule(), "Biome, light level and day count.");
        reg(new WeatherChangerModule(), "Client-side clear, rain, thunder.");
        reg(new TimeChangerModule(), "Client-side time of day.");
        reg(new TntTimerModule(), "Countdown above primed TNT.");
        reg(new WaypointsModule(), "Save and list waypoints with distance.");

        // ---------- Utility ----------
        reg(new DropPreventionModule(), "Press drop twice to throw items.");
        reg(new ToastControlModule(), "Hide pop-up toasts.");
        reg(new SubtitlesModule(), "Always show sound subtitles.");
        reg(new AutohideHudModule(), "Hide HUD when you stand still.");
        reg(new FpsBoostModule(), "One-toggle performance profile for higher FPS.");
        reg(new EntityCullingModule(), "Limit entity render distance by type.");
        reg(new ParticleLimiterModule(), "Cap particle count for smoother frames.");
        reg(new InventoryOverhaulModule(), "Smoother inventory with world context.");
        reg(new FriendsModule(), "Friends list, join alerts, LAN helpers.");
        reg(new ReconnectModule(), "Auto-reconnect after disconnect.");
        reg(new NickHiderModule(), "Hide or replace your name locally.");
        reg(new ScoreboardModule(), "Hide or scale the scoreboard.");
        reg(new ShulkerTooltipModule(), "Preview shulker box contents in tooltips.");

        // ================= Remaining (need heavy render/engine/external) =================
        Category V = Category.VISUAL, U = Category.UTILITY;
        soon("Motion Blur", V, "Smooth motion trails while turning.");
        soon("Color Saturation", V, "Tune the world's color intensity.");
        soon("Custom Fog", V, "Change fog color and distance.");
        soon("Glint", V, "Recolor the enchantment glint.");
        soon("ViewModel", V, "Move and resize your held item.");
        soon("Block Overlay", V, "Custom block outline and fill.");
        soon("Item Physic", V, "Dropped items lie on the ground.");
        soon("Loot Beams", V, "Light beams above dropped loot.");
        soon("Nametags", V, "Better, customizable nametags.");
        soon("Snaplook", V, "Hold to glance around (camera offset).");
        soon("Custom Chat", U, "Restyle and filter chat.");
        soon("Discord", U, "Rich Presence for Discord.");
        soon("Tablist", U, "Customizable player list.");
        soon("Voice", U, "Voice chat integration.");

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
        for (Module m : MODULES) {
            if (!m.enabled) continue;
            if (hudHidden && m instanceof HudModule) continue;
            m.onRender(ctx, mc);
        }
    }
}
