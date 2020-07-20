package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Timer;
import net.minecraft.client.render.RenderTickCounter;

@Mixin(RenderTickCounter.class)
public class MixinRenderTickCounter {

	@Shadow
	public float lastFrameDuration;

	@Shadow
	public int ticksThisFrame;

	@Shadow
	public float tickDelta;

	@Shadow
	private long prevTimeMillis;

	@Shadow
	private float tickTime;

	@Inject(at = @At("HEAD"), method = "beginRenderTick", cancellable = true)
	public void beginRenderTick(long long_1, CallbackInfo ci) {
		if (ModuleManager.getModule(Timer.class).isToggled()) {
			this.lastFrameDuration = (long_1 - this.prevTimeMillis) / this.tickTime;
			lastFrameDuration *= (float) ModuleManager.getModule(Timer.class).getSettings().get(0).asSlider().getValue();
			this.prevTimeMillis = long_1;
			this.tickDelta += this.lastFrameDuration;
			this.ticksThisFrame = (int)this.tickDelta;
			this.tickDelta -= this.ticksThisFrame;

			ci.cancel();
		}
	}

}
