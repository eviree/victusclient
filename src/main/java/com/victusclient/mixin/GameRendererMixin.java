package com.victusclient.mixin;

import com.victusclient.ZoomState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * getFov returns double (older versions) or float (newer ones); the raw callback handles both.
 * Same method name + (Camera, float, boolean) signature on 1.21.1 - 1.21.11.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void victus$zoom(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable cir) {
        if (!changingFov) return;
        Object v = cir.getReturnValue();
        if (v instanceof Double d) cir.setReturnValue(ZoomState.apply(d));
        else if (v instanceof Float f) cir.setReturnValue((float) ZoomState.apply(f));
    }
}
