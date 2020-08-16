package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.NoRender;
import net.minecraft.client.render.item.HeldItemRenderer;

@Mixin(HeldItemRenderer.class)
public class MixinFirstPersonRenderer {

	@Inject(at = @At("HEAD"), method = "renderFireOverlay", cancellable = true)
	private void onRenderFireOverlay(CallbackInfo ci) {
		if (ModuleManager.getModule(NoRender.class).isToggled() && ModuleManager.getModule(NoRender.class).getSetting(1).asToggle().state)
			ci.cancel();
	}

	@Inject(at = @At("HEAD"), method = "renderWaterOverlay", cancellable = true)
	private void onRenderWaterOverlay(float float_1, CallbackInfo ci) {
		if (ModuleManager.getModule(NoRender.class).isToggled() && ModuleManager.getModule(NoRender.class).getSetting(3).asToggle().state)
			ci.cancel();
	}
}
