package bleach.hack.mixin;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventSkyColor;
import net.minecraft.client.render.SkyProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkyProperties.class)
public class MixinSkyProperties {

    @Inject(at = @At("HEAD"), method = "getSkyColor", cancellable = true)
    public void getSkyColor(float skyAngle, float tickDelta, CallbackInfoReturnable<float[]> ci) {
        EventSkyColor.SkyColor event = new EventSkyColor.SkyColor(tickDelta);
        BleachHack.eventBus.post(event);
        if (event.isCancelled()) {
            ci.setReturnValue(null);
            ci.cancel();
        } else if (event.getColor() != null) {
            ci.setReturnValue(new float[]{(float) event.getColor().x, (float) event.getColor().y, (float) event.getColor().z, 1f});
            ci.cancel();
        }
    }
}
