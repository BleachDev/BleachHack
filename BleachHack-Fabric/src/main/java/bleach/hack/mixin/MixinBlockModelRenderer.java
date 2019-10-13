package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Xray;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ExtendedBlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

/**
 * Blocks are still tesselated even if they're transparent because Minecraft's rendering engine is retarded.
 */
@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer {
    @Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
    private void tesselate(ExtendedBlockView extendedBlockView_1, BakedModel bakedModel_1, BlockState blockState_1, BlockPos blockPos_1, BufferBuilder bufferBuilder_1, boolean boolean_1, Random random_1, long long_1, CallbackInfoReturnable<Boolean> ci) {
        try {
            Xray xray = (Xray) ModuleManager.getModule(Xray.class);
            if (!xray.isVisible(blockState_1.getBlock())) {
                ci.setReturnValue(false);
                ci.cancel();
            }
        } catch (Exception ignored) {}
    }

    @ModifyArg(method = "tesselate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModelRenderer;tesselateSmooth(Lnet/minecraft/world/ExtendedBlockView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/BufferBuilder;ZLjava/util/Random;J)Z"))
    private boolean tesselateSmooth(boolean checkSides) {
        try {
            if (ModuleManager.getModule(Xray.class).isToggled()) return false;
        } catch (Exception ignored) {}
        return checkSides;
    }

    @ModifyArg(method = "tesselate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModelRenderer;tesselateFlat(Lnet/minecraft/world/ExtendedBlockView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/render/BufferBuilder;ZLjava/util/Random;J)Z"))
    private boolean tesselateFlat(boolean checkSides) {
        try {
            if (ModuleManager.getModule(Xray.class).isToggled()) return false;
        } catch (Exception ignored) {}

        return checkSides;
    }
	
}
