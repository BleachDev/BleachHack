package bleach.hack.mixin;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import bleach.hack.module.ModuleManager;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.util.math.Direction;

@Mixin(FlowableFluid.class)
public class MixinFlowableFluid {

	/** Yeet the first iterator which handles the horizontal fluid movement **/
	@Redirect(method = "getVelocity", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", ordinal = 0))
	public boolean getVelocity_hasNext(Iterator<Direction> var9) {
		if (ModuleManager.getModule("NoVelocity").isEnabled()
				&& ModuleManager.getModule("NoVelocity").getSetting(3).asToggle().state) {
			return false;
		}

		return var9.hasNext();
	}

}
