package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.resource.ResourcePackProfile;
import java.util.ArrayList;
import java.util.List;

public class PackDisplayModule extends HudModule {
    public PackDisplayModule() { super("Pack Display", 6, 500); }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        List<String> ls = new ArrayList<>();
        for (ResourcePackProfile p : mc.getResourcePackManager().getEnabledProfiles()) {
            String n = p.getDisplayName().getString();
            if (n.equalsIgnoreCase("vanilla") || n.equalsIgnoreCase("Default") || n.startsWith("Fabric")) continue;
            ls.add(n);
            if (ls.size() >= 8) break;
        }
        lines(ctx, mc, ls);
    }
}
