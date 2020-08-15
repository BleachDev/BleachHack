package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.NoRender;
import net.minecraft.client.render.Camera;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class MixinCamera {

    @Inject(at = @At("HEAD"), method = "getSubmergedFluidState()Lnet/minecraft/fluid/FluidState;", cancellable = true)
    private void getSubmergedFluidState(CallbackInfoReturnable<FluidState> cir) {
        if (ModuleManager.getModule(NoRender.class).isToggled() && ModuleManager.getModule(NoRender.class).getSetting(3).asToggle().state) {
            cir.setReturnValue(Fluids.EMPTY.getDefaultState());
        }
    }
}
