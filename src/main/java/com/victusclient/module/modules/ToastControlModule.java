package com.victusclient.module.modules;

import com.victusclient.module.Category;
import com.victusclient.module.Module;
import net.minecraft.client.MinecraftClient;

/** Removes advancement / recipe / system toasts as soon as they appear. */
public class ToastControlModule extends Module {
    public ToastControlModule() { super("Toast Control", Category.UTILITY); }

    @Override public void onTick(MinecraftClient mc) { mc.getToastManager().clear(); }
}
