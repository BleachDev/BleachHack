package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Augh;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;

@Mixin(SoundSystem.class)
public class MixinSoundSystem {

	@Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
	public void play(SoundInstance soundInstance, CallbackInfo info) {
		if(ModuleManager.getModule(Augh.class).isToggled() && soundInstance != null && !soundInstance.getId().equals(Augh.AUGH_ID)) {
			System.out.println(Augh.AUGH_EVENT + " | " + soundInstance);
			if(Augh.AUGH_EVENT == null) return;
			
			info.cancel();
			
			/*MinecraftClient.getInstance().getSoundManager().play(
					new PositionedSoundInstance(Augh.AUGH_EVENT, soundInstance.getCategory(),
							soundInstance.getVolume(),
							soundInstance.getPitch(),
							soundInstance.getX(), soundInstance.getY(), soundInstance.getZ()));*/
			MinecraftClient.getInstance().getSoundManager().play(
					PositionedSoundInstance.master(Augh.AUGH_EVENT, 1f));
		}
	}
}
