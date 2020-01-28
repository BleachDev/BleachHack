package bleach.hack.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventOpenScreen;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
	
	@Inject(at = @At("HEAD"), method = "openScreen(Lnet/minecraft/client/gui/screen/Screen;)V", cancellable = true)
	public void openScreen(Screen screen, CallbackInfo info) {
		EventOpenScreen event = new EventOpenScreen(screen);
		BleachHack.eventBus.post(event);
		if(event.isCancelled()) info.cancel();
	}
}
