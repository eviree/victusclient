package com.victusclient.module.modules;

import com.victusclient.module.HudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import java.util.ArrayList;
import java.util.List;

/** Remembers where you died last. */
public class DeathInfoModule extends HudModule {
    private String where = "--";
    private int deaths;
    private boolean dead;

    public DeathInfoModule() { super("Death Info", 6, 370); }

    @Override public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        boolean now = mc.player.getHealth() <= 0f;
        if (now && !dead) {
            deaths++;
            where = mc.player.getBlockX() + " " + mc.player.getBlockY() + " " + mc.player.getBlockZ()
                    + " (" + mc.world.getRegistryKey().getValue().getPath() + ")";
        }
        dead = now;
    }

    @Override protected void draw(DrawContext ctx, MinecraftClient mc) {
        List<String> ls = new ArrayList<>();
        ls.add("Last death: " + where);
        ls.add("Deaths: " + deaths);
        lines(ctx, mc, ls);
    }
}
