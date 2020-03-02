package bleach.hack.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.ContainerProvider;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventDrawContainer;

@Mixin(ContainerScreen.class)
public abstract class MixinContainerScreen<T extends Container> extends Screen implements ContainerProvider<T> {

	public MixinContainerScreen(Container container_1, PlayerInventory playerInventory_1, Text text_1) {
		super(text_1);
	}

	@Inject(at = @At("RETURN"), method = "render(IIF)V")
	public void render(int int_1, int int_2, float float_1, CallbackInfo info) {
		EventDrawContainer event = new EventDrawContainer(
				(ContainerScreen<?>) MinecraftClient.getInstance().currentScreen, int_1, int_2); // hmm
		BleachHack.eventBus.post(event);
		if(event.isCancelled()) info.cancel();
	}
}