package bleach.hack.mixin;

import bleach.hack.BleachHack;
import bleach.hack.event.events.Event3DRender;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
	
	@Inject(at = @At("HEAD"), method = "renderHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Camera;F)V", cancellable = true)
	private void renderHand(MatrixStack matrixStack_1, Camera camera_1, float float_1, CallbackInfo info) {
		Event3DRender event = new Event3DRender();
		BleachHack.eventBus.post(event);
		if (event.isCancelled()) info.cancel();
	}
}
