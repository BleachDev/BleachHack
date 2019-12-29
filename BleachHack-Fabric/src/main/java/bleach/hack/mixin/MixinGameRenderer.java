package bleach.hack.mixin;

import bleach.hack.BleachHack;
import bleach.hack.event.events.Event3DRender;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
	
	@Inject(at = @At("HEAD"), method = "renderHand(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Camera;F)V", cancellable = true)
	private void renderHand(MatrixStack matrixStack_1, Camera camera_1, float float_1, CallbackInfo info) {
		Event3DRender event = new Event3DRender();
		BleachHack.eventBus.post(event);
		if (event.isCancelled()) info.cancel();
	}
	
	@Redirect( 
			at = @At(value = "FIELD",
				target = "Lnet/minecraft/client/options/GameOptions;fov:D",
				opcode = Opcodes.GETFIELD,
				ordinal = 0),
			method = {"getFov(Lnet/minecraft/client/render/Camera;FZ)D"})
		private double getFov(GameOptions options) {
			return bleach.hack.module.mods.Zoom.getZoom(options.fov);
		}
	
	
}
