package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Xray;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.ExtendedBlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidRenderer.class)
public class MixinFluidRenderer {
    @Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
    public void tesselate(ExtendedBlockView extendedBlockView_1, BlockPos blockPos_1, BufferBuilder bufferBuilder_1, FluidState fluidState_1, CallbackInfoReturnable<Boolean> callbackInfo) {
        Xray xray = (Xray) ModuleManager.getModule(Xray.class);
        if (xray.isToggled() && !xray.isVisible(fluidState_1.getBlockState().getBlock())) {
            callbackInfo.setReturnValue(false);
            callbackInfo.cancel();
        }
    }

    @Inject(method = "method_3344", at = @At("HEAD"), cancellable = true)
    private static void method_3344(BlockView blockView_1, BlockPos blockPos_1, Direction direction_1, float float_1, CallbackInfoReturnable<Boolean> callbackInfo) {
        Xray xray = (Xray) ModuleManager.getModule(Xray.class);
        if (xray.isToggled() && xray.isVisible(blockView_1.getBlockState(blockPos_1).getBlock())) {
            callbackInfo.setReturnValue(false);
            callbackInfo.cancel();
        }
    }
}
