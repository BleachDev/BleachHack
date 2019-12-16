package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Xray;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class MixinBlock {
	
    @Inject(method = "getAmbientOcclusionLightLevel", at = @At("HEAD"), cancellable = true)
    public void getAmbientOcclusionLightLevel(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, CallbackInfoReturnable<Float> callbackInfoReturnable) {
        if (ModuleManager.getModule(Xray.class).isToggled()) {
            callbackInfoReturnable.setReturnValue(1.0F);
        }
    }

    @Inject(method = "isShapeFullCube", at = @At("HEAD"), cancellable = true)
    private static void isShapeFullCube(VoxelShape voxelShape_1, CallbackInfoReturnable<Boolean> callback) {
        if (ModuleManager.getModule(Xray.class).isToggled()) {
            callback.setReturnValue(false);
            callback.cancel();
        }
    }

    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private static void shouldDrawSide(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, Direction direction_1, CallbackInfoReturnable<Boolean> callback) {
        try {
            Xray xray = (Xray) ModuleManager.getModule(Xray.class);
            if (xray.isToggled()) {
                callback.setReturnValue(xray.isVisible(blockState_1.getBlock()));
                callback.cancel();
            }
        } catch (Exception ignored) {}
    }

    @Inject(method = "isFullOpaque", at = @At("HEAD"), cancellable = true)
    public void isFullOpaque(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1, CallbackInfoReturnable<Boolean> callback) {
        try {
            Xray xray = (Xray) ModuleManager.getModule(Xray.class);
            if (xray.isToggled()) {
                callback.setReturnValue(xray.isVisible(blockState_1.getBlock()));
                callback.cancel();
            }
        } catch (Exception ignored) {}
    }

    /*@Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    public void getRenderType(CallbackInfoReturnable<BlockRenderType> callback) {
        try {
            if (ModuleManager.getModule(Xray.class).isToggled()) {
                callback.setReturnValue(BlockRenderType.INVISIBLE);
                callback.cancel();
            }
        } catch (Exception ignored) {}
    }*/
}
