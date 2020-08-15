package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.NoRender;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapRenderer.class)
public class MixinMapRenderer {

    @Inject(at = @At("HEAD"), method = "draw", cancellable = true)
    public void draw(MatrixStack matrixStack_1, VertexConsumerProvider vertexConsumerProvider_1, MapState mapState_1, boolean boolean_1, int int_1, CallbackInfo ci) {
        if (ModuleManager.getModule(NoRender.class).isToggled() && ModuleManager.getModule(NoRender.class).getSetting(11).asToggle().state) {
            ci.cancel();
        }
    }
}
