package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Xray;
import net.minecraft.block.AbstractBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class MixinAbstractBlockState {

	//This stops non-opaque blocks from rendering if they're fully covered by other blocks i.e clumps of leaves on trees, glass etc..
	@Inject(method = "isOpaque", at = @At("HEAD"), cancellable = true)
	public void isOpaque(CallbackInfoReturnable<Boolean> cir) {
		Xray xray = (Xray) ModuleManager.getModule("Xray");

		if(xray.isEnabled() && xray.getSetting(1).asToggle().state) {
			cir.setReturnValue(true);
		}
	}
}
