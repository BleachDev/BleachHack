package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.module.ModuleManager;
import net.minecraft.client.gui.MapRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.map.MapState;

@Mixin(MapRenderer.class)
public class MixinMapRenderer {

	@Inject(method = "draw", at = @At("HEAD"), cancellable = true)
	public void draw(MatrixStack matrices, VertexConsumerProvider vertexConsumers, MapState mapState, boolean bl, int light, CallbackInfo ci) {
		if (ModuleManager.getModule("NoRender").isEnabled() && ModuleManager.getModule("NoRender").getSetting(11).asToggle().state) {
			ci.cancel();
		}
	}
}
