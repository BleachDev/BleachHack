package org.bleachhack.util.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public class ShaderEffectWrapper {

	private static MinecraftClient mc = MinecraftClient.getInstance();

	private ShaderEffect shader;
	private int lastWidth = -1;
	private int lastHeight = -1;

	public ShaderEffectWrapper(ShaderEffect effect) {
		this.shader = effect;
	}

	public void prepare() {
		if (lastWidth != mc.getWindow().getFramebufferWidth() || lastHeight != mc.getWindow().getFramebufferHeight())
			resizeShader();
	}

	public void render() {
		shader.render(mc.getTickDelta());
		mc.getFramebuffer().beginWrite(false);
	}
	
	public Framebuffer getFramebuffer(String framebuffer) {
		return shader.getSecondaryTarget(framebuffer);
	}

	public void clearFramebuffer(String framebuffer) {
		getFramebuffer(framebuffer).clear(MinecraftClient.IS_SYSTEM_MAC);
		MinecraftClient.getInstance().getFramebuffer().beginWrite(false);
	}

	public void drawFramebufferToMain(String framebuffer) {
		Framebuffer buffer = getFramebuffer(framebuffer);
		Shader blitshader = mc.gameRenderer.blitScreenShader;
		blitshader.addSampler("DiffuseSampler", buffer.getColorAttachment());

		double w = mc.getWindow().getFramebufferWidth();
		double h = mc.getWindow().getFramebufferHeight();
		float ws = (float) buffer.viewportWidth / (float) buffer.textureWidth;
		float hs = (float) buffer.viewportHeight / (float) buffer.textureHeight;

		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);

		GlStateManager._colorMask(true, true, true, false);
		GlStateManager._disableDepthTest();
		GlStateManager._depthMask(false);
		GlStateManager._viewport(0, 0, (int) w, (int) h);

		blitshader.bind();
		Tessellator tessellator = RenderSystem.renderThreadTesselator();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferBuilder.vertex(0, h, 0).texture(0f, 0f).color(255, 255, 255, 255).next();
		bufferBuilder.vertex(w, h, 0).texture(ws, 0f).color(255, 255, 255, 255).next();
		bufferBuilder.vertex(w, 0, 0).texture(ws, hs).color(255, 255, 255, 255).next();
		bufferBuilder.vertex(0, 0, 0).texture(0f, hs).color(255, 255, 255, 255).next();
		bufferBuilder.end();
		BufferRenderer.postDraw(bufferBuilder);
		blitshader.unbind();

		GlStateManager._depthMask(true);
		GlStateManager._colorMask(true, true, true, true);

		RenderSystem.disableBlend();
	}

	private void resizeShader() {
		shader.setupDimensions(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
		lastWidth = mc.getWindow().getFramebufferWidth();
		lastHeight = mc.getWindow().getFramebufferHeight();
	}
	
	public ShaderEffect getShader() {
		return shader;
	}

	public void setShader(ShaderEffect shader) {
		this.shader = shader;
	}
}
