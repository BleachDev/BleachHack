package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import bleach.hack.module.ModuleManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@Mixin(AbstractBlock.class)
public class MixinAbstractBlock {

	@Inject(method = "getAmbientOcclusionLightLevel", at = @At("HEAD"), cancellable = true)
	public void getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> callback) {
		if (ModuleManager.getModule("Xray").isEnabled()) {
			callback.setReturnValue(1f);
		}
	}
}
