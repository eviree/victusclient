package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;

/**
 * Marks that shulker box tooltips should show contents.
 * Full implementation needs ItemTooltipCallback; this module exposes the toggle
 * and a simple status so the menu is complete. Wire Fabric item tooltip event
 * in VictusClient if the callback API is available on the target mapping.
 */
public class ShulkerTooltipModule extends Module {
    public static ShulkerTooltipModule INSTANCE;

    public final Setting.Bool compact = add(new Setting.Bool("Compact list", true));
    public final Setting.Slider maxLines = add(new Setting.Slider("Max lines", 8, 3, 20, 1));

    public ShulkerTooltipModule() {
        super("Shulker Tooltips", Category.UTILITY);
        INSTANCE = this;
    }

    public static boolean active() {
        return INSTANCE != null && INSTANCE.enabled;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        // event-driven when tooltip callback is registered
    }
}
