package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Nuker;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;

@Mixin(ParticleManager.class)
public class MixinParticleManager {

	@Inject(at = @At("HEAD"), method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", cancellable = true)
	public void addParticle(Particle particle_1, CallbackInfo ci) {
		// pls send help
		/*EventParticle event = new EventParticle(particle_1);
		BleachHack.eventBus.post(event);
		if (event.isCancelled()) ci.cancel();*/
		
		if (ModuleManager.getModule(Nuker.class).isToggled() && ModuleManager.getModule(Nuker.class).getSetting(6).asToggle().state) {
			ci.cancel();
		}
	}
}
