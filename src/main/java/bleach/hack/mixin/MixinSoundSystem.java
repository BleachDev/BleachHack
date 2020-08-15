package bleach.hack.mixin;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventSoundPlay;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.client.sound.TickableSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
public class MixinSoundSystem {

    @Inject(at = @At("HEAD"), method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", cancellable = true)
    public void play(SoundInstance soundInstance_1, CallbackInfo ci) {
        EventSoundPlay.Normal event = new EventSoundPlay.Normal(soundInstance_1);
        BleachHack.eventBus.post(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "play(Lnet/minecraft/client/sound/SoundInstance;I)V", cancellable = true)
    public void play(SoundInstance soundInstance_1, int i, CallbackInfo ci) {
        EventSoundPlay.Normal event = new EventSoundPlay.Normal(soundInstance_1);
        BleachHack.eventBus.post(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "playNextTick", cancellable = true)
    public void playNextTick(TickableSoundInstance soundInstance_1, CallbackInfo ci) {
        EventSoundPlay.Normal event = new EventSoundPlay.Normal(soundInstance_1);
        BleachHack.eventBus.post(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "addPreloadedSound", cancellable = true)
    public void addPreloadedSound(Sound sound_1, CallbackInfo ci) {
        EventSoundPlay.Preloaded event = new EventSoundPlay.Preloaded(sound_1);
        BleachHack.eventBus.post(event);
        if (event.isCancelled()) ci.cancel();
    }
}
