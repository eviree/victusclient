package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import net.minecraft.client.MinecraftClient;

public class SubtitlesModule extends Module {
    private Boolean original;

    public SubtitlesModule() { super("Subtitles", Category.UTILITY); }

    @Override public void onTick(MinecraftClient mc) {
        if (mc.options == null) return;
        if (original == null) original = mc.options.getShowSubtitles().getValue();
        if (!mc.options.getShowSubtitles().getValue()) mc.options.getShowSubtitles().setValue(true);
    }

    @Override public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (original != null && mc.options != null) mc.options.getShowSubtitles().setValue(original);
        original = null;
    }
}
