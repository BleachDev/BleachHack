package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.NoRender;
import net.minecraft.world.chunk.light.ChunkSkyLightProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSkyLightProvider.class)
public class MixinChunkSkylightProvider {

    @Inject(at = @At("HEAD"), method = "recalculateLevel", cancellable = true)
    protected void recalculateLevel(long long_1, long long_2, int int_1, CallbackInfoReturnable<Integer> ci) {
        if (ModuleManager.getModule(NoRender.class).isToggled() && ModuleManager.getModule(NoRender.class).getSetting(12).asToggle().state) {
            ci.setReturnValue(15);
            ci.cancel();
        }
    }
}
