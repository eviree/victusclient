package com.victusclient.module.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Simple saved waypoints with on-screen distance list. */
public class WaypointsModule extends Module {
    public static WaypointsModule INSTANCE;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("victusclient-waypoints.json");

    public final Setting.Bool showHud = add(new Setting.Bool("Show on HUD", true));
    public final Setting.Slider maxShow = add(new Setting.Slider("Max listed", 5, 1, 12, 1));

    public static class WP {
        public String name;
        public int x, y, z;
        public WP() {}
        public WP(String name, int x, int y, int z) {
            this.name = name; this.x = x; this.y = y; this.z = z;
        }
    }

    private final List<WP> points = new ArrayList<>();

    public WaypointsModule() {
        super("Waypoints", Category.WORLD);
        INSTANCE = this;
        load();
    }

    public synchronized void addHere(String name, MinecraftClient mc) {
        if (mc.player == null) return;
        BlockPos p = mc.player.getBlockPos();
        points.add(new WP(name == null || name.isBlank() ? "Point" : name, p.getX(), p.getY(), p.getZ()));
        save();
    }

    public synchronized void clear() {
        points.clear();
        save();
    }

    public synchronized List<WP> all() {
        return new ArrayList<>(points);
    }

    @Override
    public void onRender(DrawContext ctx, MinecraftClient mc) {
        if (!showHud.value || mc.player == null || mc.options.hudHidden) return;
        int y = 80;
        int shown = 0;
        synchronized (this) {
            for (WP wp : points) {
                if (shown >= maxShow.value) break;
                double dx = mc.player.getX() - wp.x;
                double dy = mc.player.getY() - wp.y;
                double dz = mc.player.getZ() - wp.z;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                String s = String.format("%s  %.0fm  (%d %d %d)", wp.name, dist, wp.x, wp.y, wp.z);
                ctx.drawText(mc.textRenderer, s, 6, y, 0xFFC4B5FF, true);
                y += 10;
                shown++;
            }
        }
    }

    private void load() {
        if (!Files.exists(FILE)) return;
        try (Reader r = Files.newBufferedReader(FILE)) {
            Type t = new TypeToken<List<WP>>() {}.getType();
            List<WP> list = GSON.fromJson(r, t);
            if (list != null) {
                points.clear();
                points.addAll(list);
            }
        } catch (Exception ignored) {}
    }

    private void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer w = Files.newBufferedWriter(FILE)) {
                GSON.toJson(points, w);
            }
        } catch (Exception ignored) {}
    }
}
