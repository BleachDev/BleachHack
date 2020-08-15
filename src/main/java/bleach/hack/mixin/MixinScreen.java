package bleach.hack.mixin;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventDrawTooltip;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public class MixinScreen {

    private int lastMX = 0;
    private int lastMY = 0;

    @Inject(at = @At("HEAD"), method = "render")
    public void render(MatrixStack matrix, int int_1, int int_2, float float_1, CallbackInfo info) {
        lastMX = int_1;
        lastMY = int_2;
    }

    @Inject(at = @At("HEAD"), method = "renderOrderedTooltip", cancellable = true)
    public void renderTooltip(MatrixStack matrix, List<? extends OrderedText> text, int x, int y, CallbackInfo info) {
        EventDrawTooltip event = new EventDrawTooltip(matrix, text, x, y, lastMX, lastMY);
        BleachHack.eventBus.post(event);

        if (event.isCancelled()) {
            info.cancel();
        } else if (!event.text.equals(text) || event.x != x || event.y != y) {
            event.screen.renderOrderedTooltip(matrix, event.text, event.x, event.y);
            info.cancel();
        }
    }
}
