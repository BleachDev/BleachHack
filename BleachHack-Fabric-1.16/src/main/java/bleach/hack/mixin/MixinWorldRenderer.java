package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventSkyRender;
import bleach.hack.event.events.EventWorldRenderEntity;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

	@Shadow
	private BufferBuilderStorage bufferBuilders;

	@Inject(at = @At("HEAD"), method = "renderEntity", cancellable = true)
	private void renderEntity(Entity entity_1, double double_1, double double_2, double double_3, float float_1, MatrixStack matrixStack_1,
			VertexConsumerProvider vertexConsumerProvider_1, CallbackInfo ci) {
		EventWorldRenderEntity event = new EventWorldRenderEntity(entity_1, matrixStack_1, vertexConsumerProvider_1, bufferBuilders);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			ci.cancel();
		}
	}

	@Redirect(method = "renderEndSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumer;color(IIII)Lnet/minecraft/client/render/VertexConsumer;"))
	private VertexConsumer renderEndSky_color(VertexConsumer vertexConsumer, int red, int green, int blue, int alpha) {
		EventSkyRender.Color.EndSkyColor event = new EventSkyRender.Color.EndSkyColor(1f);
		BleachHack.eventBus.post(event);

		if (event.getColor() != null) {
			return vertexConsumer.color(
					(int) (event.getColor().x * 255), (int) (event.getColor().y * 255), (int) (event.getColor().z * 255), (int) alpha);
		} else {
			return vertexConsumer.color(red, green, blue, alpha);
		}
	}

}
