package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Xray;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderLayers.class)
public class MixinRenderLayers {

	@Inject(method = "getBlockLayer", at = @At("HEAD"), cancellable = true)
	private static void getBlockLayer(BlockState state, CallbackInfoReturnable<RenderLayer> cir) {
		Xray xray = (Xray) ModuleManager.getModule("Xray");

		if (xray.isEnabled() && xray.getSetting(1).asToggle().state && !xray.isVisible(state.getBlock())) {
			cir.setReturnValue(RenderLayer.getTranslucent());
		}
	}
}
