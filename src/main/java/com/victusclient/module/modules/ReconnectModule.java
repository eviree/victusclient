package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import com.victusclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

/**
 * Auto-reconnect after a disconnect, with configurable delay.
 */
public class ReconnectModule extends Module {
    private final Setting.Slider delay = add(new Setting.Slider("Delay (s)", 3, 1, 15, 1));
    private final Setting.Bool auto = add(new Setting.Bool("Auto reconnect", true));

    private ServerInfo lastServer;
    private long disconnectAt = -1;
    private boolean pending;

    public ReconnectModule() {
        super("Reconnect", Category.UTILITY);
    }

    public void remember(ServerInfo info) {
        if (info != null) lastServer = info;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (!enabled || !auto.value) return;

        if (mc.currentScreen instanceof DisconnectedScreen) {
            if (!pending) {
                disconnectAt = System.currentTimeMillis();
                pending = true;
                // try to capture last server from network handler if available
                if (mc.getCurrentServerEntry() != null) {
                    lastServer = mc.getCurrentServerEntry();
                }
            } else if (lastServer != null && System.currentTimeMillis() - disconnectAt >= delay.value * 1000L) {
                pending = false;
                ServerAddress addr = ServerAddress.parse(lastServer.address);
                ConnectScreen.connect(new TitleScreen(), mc, addr, lastServer, false, null);
            }
        } else {
            pending = false;
        }
    }
}
