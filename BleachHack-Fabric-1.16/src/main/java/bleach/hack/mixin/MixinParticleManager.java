package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventParticle;
import bleach.hack.module.ModuleManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;

@Mixin(ParticleManager.class)
public class MixinParticleManager {

	@Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
	public void addParticle(Particle particle, CallbackInfo ci) {
		// pls send help
		EventParticle.Normal event = new EventParticle.Normal(particle);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			ci.cancel();
		}
	}

	@Inject(method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;)V", at = @At("HEAD"), cancellable = true)
	public void addEmitter(Entity entity, ParticleEffect particleEffect, CallbackInfo ci) {
		EventParticle.Emitter event = new EventParticle.Emitter(particleEffect);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			ci.cancel();
		}
	}

	@Inject(method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;I)V", at = @At("HEAD"), cancellable = true)
	public void addEmitter_(Entity entity, ParticleEffect particleEffect, int maxAge, CallbackInfo ci) {
		EventParticle.Emitter event = new EventParticle.Emitter(particleEffect);
		BleachHack.eventBus.post(event);
		if (event.isCancelled()) {
			ci.cancel();
		}
	}

	/** Mojang is retarded and makes this and ONLY this thread-unsafe **/
	@Inject(method = "addBlockBreakParticles", at = @At("HEAD"), cancellable = true)
	public void addBlockBreakParticles(BlockPos pos, BlockState state, CallbackInfo ci) {
		if (ModuleManager.getModule("Nuker").isEnabled() && ModuleManager.getModule("Nuker").getSetting(7).asToggle().state) {
			ci.cancel();
		}
	}
}