package bleach.hack.mixin;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventParticle;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Nuker;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class MixinParticleManager {

    @Inject(at = @At("HEAD"), method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", cancellable = true)
    public void addParticle(Particle particle_1, CallbackInfo ci) {
        // pls send help
        EventParticle.Normal event = new EventParticle.Normal(particle_1);
        BleachHack.eventBus.post(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;)V", cancellable = true)
    public void addEmitter(Entity entity_1, ParticleEffect particleEffect_1, CallbackInfo ci) {
        EventParticle.Emitter event = new EventParticle.Emitter(particleEffect_1);
        BleachHack.eventBus.post(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;I)V", cancellable = true)
    public void addEmitter1(Entity entity_1, ParticleEffect particleEffect_1, int i, CallbackInfo ci) {
        EventParticle.Emitter event = new EventParticle.Emitter(particleEffect_1);
        BleachHack.eventBus.post(event);
        if (event.isCancelled()) ci.cancel();
    }

    /**
     * Mojang is retarded and makes this and ONLY this thread-unsafe
     **/
    @Inject(at = @At("HEAD"), method = "addBlockBreakParticles", cancellable = true)
    public void addBlockBreakParticles(BlockPos blockPos_1, BlockState blockState_1, CallbackInfo ci) {
        if (ModuleManager.getModule(Nuker.class).isToggled() && ModuleManager.getModule(Nuker.class).getSetting(6).asToggle().state) {
            ci.cancel();
        }
    }
}