package bleach.hack.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventDrawTooltip;
import net.minecraft.client.gui.screen.Screen;

@Mixin(Screen.class)
public class MixinScreen {

	private int lastMX = 0;
	private int lastMY = 0;

	@Inject(at = @At("HEAD"), method = "render(IIF)V")
	public void render(int int_1, int int_2, float float_1, CallbackInfo info) {
		lastMX = int_1;
		lastMY = int_2;
	}

	@Inject(at = @At("HEAD"), method = "renderTooltip(Ljava/util/List;II)V", cancellable = true)
	public void renderTooltip(List<String> text, int x, int y, CallbackInfo info) {
		EventDrawTooltip event = new EventDrawTooltip(text, x, y, lastMX, lastMY);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			info.cancel();
		} else if (!event.text.equals(text) || event.x != x || event.y != y) {
			event.screen.renderTooltip(event.text, event.x, event.y);
			info.cancel();
		}
	}
}
