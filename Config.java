package com.victusclient.config;

import com.google.gson.*;
import com.victusclient.module.HudModule;
import com.victusclient.module.Module;
import com.victusclient.module.ModuleManager;
import com.victusclient.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("victusclient.json"); }

    public static void save() {
        try {
            JsonObject root = new JsonObject();
            for (Module m : ModuleManager.all()) {
                JsonObject o = new JsonObject();
                o.addProperty("enabled", m.enabled);
                o.addProperty("favorite", m.favorite);
                if (m instanceof HudModule h) { o.addProperty("x", h.x); o.addProperty("y", h.y); }
                JsonObject s = new JsonObject();
                for (Setting st : m.settings) s.add(st.name, st.save());
                o.add("settings", s);
                root.add(m.name, o);
            }
            Files.writeString(path(), GSON.toJson(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        try {
            if (!Files.exists(path())) return;
            JsonObject root = JsonParser.parseString(Files.readString(path())).getAsJsonObject();
            for (Module m : ModuleManager.all()) {
                if (!root.has(m.name)) continue;
                JsonObject o = root.getAsJsonObject(m.name);
                if (o.has("enabled")) m.setEnabled(o.get("enabled").getAsBoolean());
                if (o.has("favorite")) m.favorite = o.get("favorite").getAsBoolean();
                if (m instanceof HudModule h) {
                    if (o.has("x")) h.x = o.get("x").getAsInt();
                    if (o.has("y")) h.y = o.get("y").getAsInt();
                }
                if (o.has("settings")) {
                    JsonObject s = o.getAsJsonObject("settings");
                    for (Setting st : m.settings) if (s.has(st.name)) st.load(s.get(st.name));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
