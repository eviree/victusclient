package com.victusclient.module.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Friends system for Victus Client.
 *
 * - Local friends list (saved to config)
 * - Quick add/remove from tab list or by name
 * - Highlight friends in the player list / chat when possible
 *
 * World joining note (important):
 * Minecraft online-mode servers require valid Microsoft authentication.
 * A pure client mod cannot legally force a cracked account onto an online-mode
 * server or a premium singleplayer world that is opened to LAN with online-mode.
 * What this module *does* provide:
 *  - friends list management
 *  - easy LAN open helper messaging
 *  - invite-style chat commands the host can use
 * For mixed premium / offline play, the host must open to LAN with cheats/online-mode
 * settings that match their group, or use a dedicated offline-friendly server.
 */
public class FriendsModule extends Module {
    public static FriendsModule INSTANCE;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("victusclient-friends.json");

    public final Setting.Bool highlightTab = add(new Setting.Bool("Highlight in tab", true));
    public final Setting.Bool notifyJoin = add(new Setting.Bool("Notify on join", true));
    public final Setting.Bool prefixChat = add(new Setting.Bool("Friend prefix in chat", false));

    private final Set<String> friends = new LinkedHashSet<>();
    private final Set<String> onlineNotified = new LinkedHashSet<>();

    public FriendsModule() {
        super("Friends", Category.UTILITY);
        INSTANCE = this;
        load();
    }

    public synchronized List<String> list() {
        return new ArrayList<>(friends);
    }

    public synchronized boolean isFriend(String name) {
        if (name == null) return false;
        return friends.contains(name.toLowerCase(Locale.ROOT));
    }

    public synchronized boolean addFriend(String name) {
        if (name == null || name.isBlank()) return false;
        boolean ok = friends.add(name.trim().toLowerCase(Locale.ROOT));
        if (ok) save();
        return ok;
    }

    public synchronized boolean removeFriend(String name) {
        if (name == null) return false;
        boolean ok = friends.remove(name.trim().toLowerCase(Locale.ROOT));
        if (ok) save();
        return ok;
    }

    public synchronized void clearFriends() {
        friends.clear();
        save();
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!enabled || mc.getNetworkHandler() == null || mc.player == null) return;
        if (!notifyJoin.value) return;

        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            String name = entry.getProfile().getName();
            if (name == null) continue;
            String key = name.toLowerCase(Locale.ROOT);
            if (isFriend(key) && onlineNotified.add(key)) {
                mc.player.sendMessage(Text.literal("§d[Victus] §fFriend online: §b" + name), false);
            }
        }
        // prune offline
        onlineNotified.removeIf(n -> mc.getNetworkHandler().getPlayerList().stream()
                .noneMatch(e -> n.equalsIgnoreCase(e.getProfile().getName())));
    }

    private void load() {
        if (!Files.exists(FILE)) return;
        try (Reader r = Files.newBufferedReader(FILE)) {
            Type t = new TypeToken<List<String>>() {}.getType();
            List<String> list = GSON.fromJson(r, t);
            if (list != null) {
                friends.clear();
                for (String s : list) {
                    if (s != null && !s.isBlank()) friends.add(s.toLowerCase(Locale.ROOT));
                }
            }
        } catch (IOException ignored) {}
    }

    private void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer w = Files.newBufferedWriter(FILE)) {
                GSON.toJson(new ArrayList<>(friends), w);
            }
        } catch (IOException ignored) {}
    }

    /** Helper text shown in the menu info panel. */
    public static String helpText() {
        return "Add friends by name. Hosts: open to LAN so friends can join. "
                + "Online-mode servers still require valid accounts — Victus cannot bypass Microsoft auth.";
    }
}
