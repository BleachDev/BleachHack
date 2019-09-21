package bleach.hack.mixin;

import bleach.hack.event.events.EventBlockRender;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ExtendedBlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(BlockRenderManager.class)
public class MixinBlockRenderManager {
    @Inject(method = "tesselateBlock", at = @At("HEAD"), cancellable = true)
    public void tesselateBlock(BlockState blockState_1, BlockPos blockPos_1, ExtendedBlockView extendedBlockView_1, BufferBuilder bufferBuilder_1, Random random_1, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        EventBlockRender eventBlockRender = new EventBlockRender(blockState_1, blockPos_1);
        if(eventBlockRender.isCancelled()) {
            callbackInfoReturnable.setReturnValue(false);
            callbackInfoReturnable.cancel();
        }
    }
}
