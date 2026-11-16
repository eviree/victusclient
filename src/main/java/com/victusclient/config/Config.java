package com.victusclient.config;

import com.google.gson.*;
import com.victusclient.GlobalSettings;
import com.victusclient.module.HudModule;
import com.victusclient.module.Module;
import com.victusclient.module.ModuleManager;
import com.victusclient.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    public static final String[] PROFILES = {"Default", "PvP", "Survival", "Builder", "Custom"};
    public static String profile = "Default";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path dir() { return FabricLoader.getInstance().getConfigDir(); }
    private static Path profileFile(String p) {
        return dir().resolve(p.equals("Default") ? "victusclient.json" : "victusclient-" + p.toLowerCase() + ".json");
    }
    private static Path globalFile() { return dir().resolve("victusclient-global.json"); }

    public static void save() { saveGlobal(); saveProfile(profile); }
    public static void load() { loadGlobal(); loadProfile(profile); }

    // ---- profile (modules) ----
    public static void saveProfile(String p) {
        try {
            JsonObject root = new JsonObject();
            for (Module m : ModuleManager.all()) {
                if (m.soon) continue;
                JsonObject o = new JsonObject();
                o.addProperty("enabled", m.enabled);
                o.addProperty("favorite", m.favorite);
                if (m instanceof HudModule h) { o.addProperty("x", h.x); o.addProperty("y", h.y); }
                JsonObject s = new JsonObject();
                for (Setting st : m.settings) s.add(st.name, st.save());
                o.add("settings", s);
                root.add(m.name, o);
            }
            Files.writeString(profileFile(p), GSON.toJson(root));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void loadProfile(String p) {
        try {
            if (!Files.exists(profileFile(p))) return;
            JsonObject root = JsonParser.parseString(Files.readString(profileFile(p))).getAsJsonObject();
            for (Module m : ModuleManager.all()) {
                if (m.soon || !root.has(m.name)) continue;
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ---- global (current profile name + client-wide settings) ----
    private static void saveGlobal() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("profile", profile);
            JsonObject s = new JsonObject();
            for (Setting st : GlobalSettings.ALL) s.add(st.name, st.save());
            root.add("settings", s);
            Files.writeString(globalFile(), GSON.toJson(root));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static void loadGlobal() {
        try {
            if (!Files.exists(globalFile())) return;
            JsonObject root = JsonParser.parseString(Files.readString(globalFile())).getAsJsonObject();
            if (root.has("profile")) profile = root.get("profile").getAsString();
            if (root.has("settings")) {
                JsonObject s = root.getAsJsonObject("settings");
                for (Setting st : GlobalSettings.ALL) if (s.has(st.name)) st.load(s.get(st.name));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
