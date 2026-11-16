package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Soft entity distance limits to cut render cost on dense servers / farms.
 * Does not remove entities from the world — only reduces how far the client
 * bothers rendering certain classes when the module is enabled.
 */
public class EntityCullingModule extends Module {
    public static EntityCullingModule INSTANCE;

    public final Setting.Slider maxHostile = add(new Setting.Slider("Hostile range", 48, 16, 128, 4));
    public final Setting.Slider maxPassive = add(new Setting.Slider("Passive range", 32, 8, 96, 4));
    public final Setting.Slider maxItems = add(new Setting.Slider("Item range", 24, 8, 64, 4));
    public final Setting.Slider maxOther = add(new Setting.Slider("Other range", 64, 16, 128, 4));
    public final Setting.Bool keepPlayers = add(new Setting.Bool("Always show players", true));

    public EntityCullingModule() {
        super("Entity Culling", Category.UTILITY);
        INSTANCE = this;
    }

    /** Called from mixin / render path if present; safe no-op otherwise. */
    public static boolean shouldRender(Entity e, double distSq) {
        EntityCullingModule m = INSTANCE;
        if (m == null || !m.enabled) return true;
        if (e instanceof PlayerEntity && m.keepPlayers.value) return true;

        double max;
        if (e instanceof HostileEntity) max = m.maxHostile.value;
        else if (e instanceof PassiveEntity) max = m.maxPassive.value;
        else if (e instanceof ItemEntity) max = m.maxItems.value;
        else max = m.maxOther.value;

        return distSq <= max * max;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        // marker module; heavy work is distance checks at render time
    }
}
