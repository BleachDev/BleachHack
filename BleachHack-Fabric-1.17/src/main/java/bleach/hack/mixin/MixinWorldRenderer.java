package bleach.hack.mixin;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventBlockEntityRender;
import bleach.hack.event.events.EventEntityRender;
import bleach.hack.event.events.EventSkyRender;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.mixinterface.IMixinWorldRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.profiler.Profiler;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer implements IMixinWorldRenderer {

	@Shadow private BufferBuilderStorage bufferBuilders;
	@Shadow private Framebuffer entityOutlinesFramebuffer;
	@Shadow private ShaderEffect entityOutlineShader;

	/** Fixes that the outline framebuffer only resets if any glowing entites are drawn **/
	@ModifyVariable(method = "render", at = @At(value = "STORE"), index = 37)
	//@ModifyVariable(method = "render", name = "bl3", at = @At("STORE"))
	public boolean render_modifyBoolean(boolean bool) {
		return true;
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V"))
	private void render_swap(Profiler profiler, String string) {
		if (string.equals("entities")) {
			BleachHack.eventBus.post(new EventEntityRender.PreAll());
		} else if (string.equals("blockentities")) {
			BleachHack.eventBus.post(new EventEntityRender.PostAll());
			BleachHack.eventBus.post(new EventBlockEntityRender.PreAll());
		} else if (string.equals("blockentities")) {
			BleachHack.eventBus.post(new EventEntityRender.PostAll());
			BleachHack.eventBus.post(new EventBlockEntityRender.PreAll());
		} else if (string.equals("destroyProgress")) {
			BleachHack.eventBus.post(new EventBlockEntityRender.PostAll());
		}
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void render_head(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
			LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo info) {
		EventWorldRender.Pre event = new EventWorldRender.Pre(tickDelta);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			info.cancel();
		}
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void render_return(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
			LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo info) {
		RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
		EventWorldRender.Post event = new EventWorldRender.Post(tickDelta);
		BleachHack.eventBus.post(event);
	}
	
	@Redirect(method = "renderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;render(Lnet/minecraft/entity/Entity;DDDFFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"))
	public <E extends Entity> void renderEntity_render(EntityRenderDispatcher dispatcher, E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		EventEntityRender.Single.Pre event = new EventEntityRender.Single.Pre(entity, matrices, vertexConsumers);
		BleachHack.eventBus.post(event);

		if (!event.isCancelled()) {
			dispatcher.render(event.getEntity(), x, y, z, yaw, tickDelta, event.getMatrix(), event.getVertex(), light);
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
	
	/*@Redirect(method = "method_34808", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;chunkCullingEnabled:Z", ordinal = 0),
			require = 0 / TODO: sodium? /)
	private boolean method_34808_chunkCullingEnabled(MinecraftClient client) {
		EventChunkCulling event = new EventChunkCulling(client.chunkCullingEnabled);
		BleachHack.eventBus.post(event);
		
		return event.shouldCull();
	}*/

	@Override
	public Framebuffer getOutlineFramebuffer() {
		return entityOutlinesFramebuffer;
	}

	@Override
	public void setOutlineFramebuffer(Framebuffer framebuffer) {
		this.entityOutlinesFramebuffer = framebuffer;
	}

	@Override
	public ShaderEffect getOutlineShader() {
		return entityOutlineShader;
	}

	@Override
	public void setOutlineShader(ShaderEffect shader) {
		this.entityOutlineShader = shader;
	}

	/*@Redirect(method = "loadEntityOutlineShader", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"))
	private Identifier loadEntityOutlineShader_newIdentifier(String id) {
		nigeria
		return new Identifier("bleachhack", "shaders/post/bh_entity_outline.json");
	}*/

}
