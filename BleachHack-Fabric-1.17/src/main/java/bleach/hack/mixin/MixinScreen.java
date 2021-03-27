package bleach.hack.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventDrawTooltip;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;

@Mixin(Screen.class)
public class MixinScreen {
 
	@Unique private int lastMX = 0;
	@Unique private int lastMY = 0;
	
	@Unique private boolean skipTooltip = false;

	@Inject(method = "render", at = @At("HEAD"))
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info) {
		lastMX = mouseX;
		lastMY = mouseY;
	}

	@Inject(at = @At("HEAD"), method = "renderOrderedTooltip", cancellable = true)
	public void renderTooltip(MatrixStack matrices, List<? extends OrderedText> lines, int x, int y, CallbackInfo info) {
		if (skipTooltip) {
			skipTooltip = false;
			return;
		}
		
		EventDrawTooltip event = new EventDrawTooltip(matrices, lines, x, y, lastMX, lastMY);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			info.cancel();
		} else if (!event.text.equals(lines) || event.x != x || event.y != y) {
			skipTooltip = true;
			event.screen.renderOrderedTooltip(matrices, event.text, event.x, event.y);
			info.cancel();
		}
	}
}
