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

        // ---------- Visual ----------
        reg(new FullbrightModule(), "Full brightness, no more dark.");
        reg(new ZoomModule(), "Smooth zoom. Hold C, scroll to adjust.");
        reg(new FovChangerModule(), "FOV beyond the vanilla limit.");
        reg(new AutoPerspectiveModule(), "Third person on elytra & rides.");

        // ---------- Movement ----------
        reg(new ToggleSprintModule(), "Sprint without holding the key.");

        // ---------- World ----------
        reg(new WorldInfoModule(), "Biome, light level and day count.");
        reg(new WeatherChangerModule(), "Client-side clear, rain, thunder.");

        // ---------- Utility ----------
        reg(new DropPreventionModule(), "Press drop twice to throw items.");
        reg(new ToastControlModule(), "Hide pop-up toasts.");
        reg(new SubtitlesModule(), "Always show sound subtitles.");
        reg(new AutohideHudModule(), "Hide HUD when you stand still.");

        // ================= ROADMAP (need deeper render / game hooks) =================
        Category V = Category.VISUAL, M = Category.MOVEMENT, W = Category.WORLD, U = Category.UTILITY;
        soon("Motion Blur", V, "Smooth motion trails while turning.");
        soon("Color Saturation", V, "Tune the world's color intensity.");
        soon("Custom Fog", V, "Change fog color and distance.");
        soon("Glint", V, "Recolor the enchantment glint.");
        soon("Hitbox", V, "Show entity hitboxes.");
        soon("ViewModel", V, "Move and resize your held item.");
        soon("Block Overlay", V, "Custom block outline and fill.");
        soon("Item Physic", V, "Dropped items lie on the ground.");
        soon("Loot Beams", V, "Light beams above dropped loot.");
        soon("Nametags", V, "Better, customizable nametags.");
        soon("Hearts", V, "Custom health hearts.");
        soon("Armor Bar", V, "Custom armor bar.");
        soon("Boss Bar", V, "Hide or restyle the boss bar.");
        soon("Custom Crosshair", V, "Design your own crosshair.");
        soon("Player Model", V, "Tweak your player model.");
        soon("Camera", V, "Camera distance and smoothing.");
        soon("Perspective", V, "Freelook around your player.");
        soon("Snaplook", V, "Hold to glance around.");
        soon("Dark Mode", V, "Dark theme for game screens.");
        soon("Elytras", M, "Elytra flight helpers and HUD.");
        soon("Horses", M, "Horse stats while riding.");
        soon("Time Changer", W, "Set the time of day client-side.");
        soon("TNT Timer", W, "Countdown above primed TNT.");
        soon("Waypoints", W, "Mark and share locations.");
        soon("Item Despawn", W, "Despawn timer for dropped items.");
        soon("Light Level Overlay", W, "Show spawnable light levels.");
        soon("Mob Overlay", W, "Highlight mob spawn spots.");
        soon("Auto Text", U, "Send saved chat messages with a key.");
        soon("Backups", U, "Automatic world backups.");
        soon("Cull Logs", U, "Trim and clean old log files.");
        soon("Custom Advancements Screen", U, "Redesigned advancements UI.");
        soon("Custom Chat", U, "Restyle and filter chat.");
        soon("Custom F3", U, "Cleaner debug screen.");
        soon("Discord", U, "Rich Presence for Discord.");
        soon("Hypixel", U, "Hypixel-specific tools.");
        soon("Keybind Search", U, "Search in the keybind screen.");
        soon("Nick Hider", U, "Hide your name from chat and tab.");
        soon("Pack Organizer", U, "Manage resource packs easily.");
        soon("Reconnect", U, "Auto-reconnect after disconnect.");
        soon("Scoreboard", U, "Move and restyle the scoreboard.");
        soon("Screenshot", U, "Screenshot manager and quick share.");
        soon("Shulker Tooltips", U, "Preview shulker contents.");
        soon("Sound Filters", U, "Mute or boost specific sounds.");
        soon("Tablist", U, "Customizable player list.");
        soon("Team Tracker", U, "Track your teammates.");
        soon("Tier Tagger", U, "Show PvP tiers on nametags.");
        soon("Title Tweaker", U, "Resize and move on-screen titles.");
        soon("Tooltips", U, "Extra item tooltip details.");
        soon("TPS", U, "Server tick rate display.");
        soon("UHC Overlay", U, "Tools for UHC matches.");
        soon("UI Scaling", U, "Fine-grained GUI scale.");
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
