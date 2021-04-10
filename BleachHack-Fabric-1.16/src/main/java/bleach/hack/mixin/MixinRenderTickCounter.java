package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import bleach.hack.module.ModuleManager;
import net.minecraft.client.render.RenderTickCounter;

@Mixin(RenderTickCounter.class)
public class MixinRenderTickCounter {

	@Shadow public float lastFrameDuration;
	@Shadow public float tickDelta;
	@Shadow private long prevTimeMillis;
	@Shadow private float tickTime;

	@Inject(method = "beginRenderTick", at = @At("HEAD"), cancellable = true)
	public void beginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> ci) {
		if (ModuleManager.getModule("Timer").isEnabled()) {
			this.lastFrameDuration = (float) (((timeMillis - this.prevTimeMillis) / this.tickTime)
					* ModuleManager.getModule("Timer").getSetting(0).asSlider().getValue());
			this.prevTimeMillis = timeMillis;
			this.tickDelta += this.lastFrameDuration;
			int i = (int) this.tickDelta;
			this.tickDelta -= i;

			ci.setReturnValue(i);
		}
	}

}
