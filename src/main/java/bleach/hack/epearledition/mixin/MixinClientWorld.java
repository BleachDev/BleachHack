package bleach.hack.epearledition.mixin;

import bleach.hack.epearledition.BleachHack;
import bleach.hack.epearledition.event.events.EventSkyColor;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class MixinClientWorld {

    @Inject(at = @At("HEAD"), method = "method_23777", cancellable = true)
    public void method_23777(BlockPos blockPos, float f, CallbackInfoReturnable<Vec3d> ci) {
        EventSkyColor.SkyColor event = new EventSkyColor.SkyColor(f);
        BleachHack.eventBus.post(event);
        if (event.isCancelled()) {
            ci.setReturnValue(Vec3d.ZERO);
            ci.cancel();
        } else if (event.getColor() != null) {
            ci.setReturnValue(event.getColor());
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "getCloudsColor", cancellable = true)
    public void getCloudsColor(float f, CallbackInfoReturnable<Vec3d> ci) {
        EventSkyColor.CloudColor event = new EventSkyColor.CloudColor(f);
        BleachHack.eventBus.post(event);
        if (event.isCancelled()) {
            ci.setReturnValue(Vec3d.ZERO);
            ci.cancel();
        } else if (event.getColor() != null) {
            ci.setReturnValue(event.getColor());
            ci.cancel();
        }
    }
}
