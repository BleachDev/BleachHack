package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Xray;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkOcclusionDataBuilder.class)
public class MixinChunkOcclusionGraphBuilder {
	
    //Forces chunks to render regardless of assumed occlusion. Without this you'd see blocks on xray disappear from your view based on your angle, even if they're in your FOV.
    @Inject(method = "markClosed", at = @At("HEAD"), cancellable = true)
    public void markClosed(BlockPos pos, CallbackInfo callback) {
        try {
            if (ModuleManager.getModule(Xray.class).isToggled()) {
                callback.cancel();
            }
        } catch (Exception ignored) {}
    }
}
