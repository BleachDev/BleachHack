package bleach.hack.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventEntityRender;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity> {

	@Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", cancellable = true)
	public void render(T entity_1, float float_1, float float_2, MatrixStack matrixStack_1, VertexConsumerProvider vertexConsumerProvider_1, int int_1, CallbackInfo info) {
		EventEntityRender.Render event = new EventEntityRender.Render((Entity) entity_1);
		BleachHack.eventBus.post(event);
		if (event.isCancelled()) info.cancel();
	}
	
	@Inject(at = @At("HEAD"), method = "renderLabelIfPresent(Lnet/minecraft/entity/Entity;Ljava/lang/String;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", cancellable = true)
	public void renderLabelIfPresent(T entity_1, String string_1, MatrixStack matrixStack_1, VertexConsumerProvider vertexConsumerProvider_1, int int_1, CallbackInfo info) {
		EventEntityRender.Label event = new EventEntityRender.Label((Entity) entity_1);
		BleachHack.eventBus.post(event);
		if (event.isCancelled()) info.cancel();
	}
}
