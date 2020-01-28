package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Xray;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

/**
 * Blocks are still tesselated even if they're transparent because Minecraft's rendering engine is retarded.
 */
@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render(BlockRenderView blockRenderView_1, BakedModel bakedModel_1, BlockState blockState_1, BlockPos blockPos_1, MatrixStack matrixStack_1, VertexConsumer vertexConsumer_1, boolean boolean_1, Random random_1, long long_1, int int_1, CallbackInfoReturnable<Boolean> ci) {
        try {
            Xray xray = (Xray) ModuleManager.getModule(Xray.class);
            if (!xray.isVisible(blockState_1.getBlock())) {
                ci.setReturnValue(false);
                ci.cancel();
            }
        } catch (Exception ignored) {}
    }

    /*@ModifyArg(method = "render", at = @At(value = "HEAD", target = "Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/Random;JI)Z"))
    private boolean renderSmooth(boolean boolean_1) {
        try {
            if (ModuleManager.getModule(Xray.class).isToggled()) return false;
        } catch (Exception ignored) {}
        return boolean_1;
    }

    @ModifyArg(method = "render", at = @At(value = "HEAD", target = "Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/client/render/model/BakedModel;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;ZLjava/util/Random;JI)Z"))
    private boolean renderFlat(boolean boolean_1) {
        try {
            if (ModuleManager.getModule(Xray.class).isToggled()) return false;
        } catch (Exception ignored) {}

        return boolean_1;
    }*/
	
}
