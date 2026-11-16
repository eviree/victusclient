package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.HudModule;
import com.victusclient.setting.Setting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;

/**
 * Simple client-side minimap. Samples top blocks in a radius around the player
 * and draws a circular / square map with a direction indicator.
 */
public class MinimapModule extends HudModule {
    private final Setting.Slider size = add(new Setting.Slider("Size", 96, 64, 160, 8));
    private final Setting.Slider zoom = add(new Setting.Slider("Zoom", 1.0, 0.5, 2.5, 0.1));
    private final Setting.Bool circle = add(new Setting.Bool("Circular", true));
    private final Setting.Bool rotate = add(new Setting.Bool("Rotate with player", true));
    private final Setting.Bool showCoords = add(new Setting.Bool("Show coords", true));
    private final Setting.Bool northUp = add(new Setting.Bool("North up", false));

    private final int[] pixels = new int[256 * 256];
    private int lastCx = Integer.MIN_VALUE, lastCz = Integer.MIN_VALUE;
    private long lastSample;

    public MinimapModule() {
        super("Minimap", Category.HUD, 8, 80);
        w = 100;
        h = 110;
    }

    @Override
    protected void draw(DrawContext ctx, MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;

        int mapSize = (int) size.value;
        w = mapSize + 8;
        h = mapSize + (showCoords.value ? 22 : 8);

        int px = x + 4;
        int py = y + 4;

        // background frame
        if (background.value) {
            int bg = bgColor();
            ctx.fill(x, y, x + w, y + h, bg);
            ctx.fill(px - 1, py - 1, px + mapSize + 1, py + mapSize + 1, 0x66000000);
        }

        sampleIfNeeded(mc);

        float yaw = mc.player.getYaw();
        float rot = rotate.value && !northUp.value ? -yaw : 0f;

        // draw map pixels (simple nearest sampling into a square, then optional circle mask)
        int half = mapSize / 2;
        int cx = mc.player.getBlockX();
        int cz = mc.player.getBlockZ();
        float scale = (float) zoom.value;

        for (int dy = 0; dy < mapSize; dy++) {
            for (int dx = 0; dx < mapSize; dx++) {
                if (circle.value) {
                    int rdx = dx - half;
                    int rdy = dy - half;
                    if (rdx * rdx + rdy * rdy > half * half) continue;
                }

                float lx = (dx - half) / scale;
                float lz = (dy - half) / scale;

                if (rotate.value && !northUp.value) {
                    float rad = (float) Math.toRadians(rot);
                    float cos = MathHelper.cos(rad);
                    float sin = MathHelper.sin(rad);
                    float rx = lx * cos - lz * sin;
                    float rz = lx * sin + lz * cos;
                    lx = rx;
                    lz = rz;
                }

                int wx = cx + MathHelper.floor(lx);
                int wz = cz + MathHelper.floor(lz);
                int col = sampleColor(mc.world, wx, wz);
                ctx.fill(px + dx, py + dy, px + dx + 1, py + dy + 1, col | 0xFF000000);
            }
        }

        // player marker (center)
        int mx = px + half;
        int my = py + half;
        ctx.fill(mx - 2, my - 2, mx + 2, my + 2, 0xFFFFFFFF);
        // direction triangle
        float dir = northUp.value ? -yaw : 0f;
        float rad = (float) Math.toRadians(dir);
        int tipX = mx + (int) (MathHelper.sin(rad) * 6);
        int tipY = my - (int) (MathHelper.cos(rad) * 6);
        ctx.fill(tipX - 1, tipY - 1, tipX + 1, tipY + 1, 0xFFFF5555);

        // border
        int border = 0xFF7C5CFF;
        if (circle.value) {
            // approximate ring
            for (int a = 0; a < 360; a += 4) {
                float r = (float) Math.toRadians(a);
                int bx = mx + (int) (MathHelper.cos(r) * half);
                int by = my + (int) (MathHelper.sin(r) * half);
                ctx.fill(bx, by, bx + 1, by + 1, border);
            }
        } else {
            ctx.fill(px, py, px + mapSize, py + 1, border);
            ctx.fill(px, py + mapSize - 1, px + mapSize, py + mapSize, border);
            ctx.fill(px, py, px + 1, py + mapSize, border);
            ctx.fill(px + mapSize - 1, py, px + mapSize, py + mapSize, border);
        }

        if (showCoords.value) {
            String s = String.format("%d, %d", cx, cz);
            ctx.drawText(mc.textRenderer, s, x + 4, y + h - 12, textColor(), shadow.value);
        }
    }

    private void sampleIfNeeded(MinecraftClient mc) {
        // sampling is done live per-pixel for simplicity and correctness with zoom/rotate
    }

    private int sampleColor(ClientWorld world, int x, int z) {
        WorldChunk chunk = world.getChunk(x >> 4, z >> 4);
        if (chunk == null || chunk.isEmpty()) return 0xFF1A1A2E;

        int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
        if (y <= world.getBottomY()) return 0xFF0A0A12;

        BlockPos.Mutable pos = new BlockPos.Mutable(x, y, z);
        BlockState state = world.getBlockState(pos);
        // skip leaves / air-ish for cleaner map
        int guard = 0;
        while ((state.isAir() || !state.isOpaque()) && guard++ < 8) {
            pos.setY(pos.getY() - 1);
            state = world.getBlockState(pos);
        }

        MapColor mapColor = state.getMapColor(world, pos);
        if (mapColor == null || mapColor == MapColor.CLEAR) {
            return 0xFF2A2A3A;
        }
        int rgb = mapColor.color;
        // slight darken for contrast
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        r = (int) (r * 0.85f);
        g = (int) (g * 0.85f);
        b = (int) (b * 0.85f);
        return (r << 16) | (g << 8) | b;
    }
}
