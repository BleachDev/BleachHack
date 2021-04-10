package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import bleach.hack.module.ModuleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;

@Mixin(Camera.class)
public class MixinCamera {

	@Inject(method = "getSubmersionType", at = @At("HEAD"), cancellable = true)
	private void getSubmergedFluidState(CallbackInfoReturnable<CameraSubmersionType> ci) {
		if (ModuleManager.getModule("NoRender").isEnabled() && ModuleManager.getModule("NoRender").getSetting(3).asToggle().state) {
			ci.setReturnValue(CameraSubmersionType.NONE);
		}
	}
}
