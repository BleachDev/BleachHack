package org.bleachhack.util.shader;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.FixedColorVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase.TextureBase;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumers;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class ColorVertexConsumerProvider {

	private final VertexConsumerProvider.Immediate plainDrawer = VertexConsumerProvider.immediate(new BufferBuilder(256));

	private Supplier<Shader> shader;
	private Function<TextureBase, RenderLayer> layerCreator;

	public ColorVertexConsumerProvider(Framebuffer framebuffer, Supplier<Shader> shader) {
		this.shader = shader;
		setFramebuffer(framebuffer);
	}
	
	public VertexConsumerProvider createDualProvider(VertexConsumerProvider parent, int red, int green, int blue, int alpha) {
		return layer -> {
			VertexConsumer parentBuffer = parent.getBuffer(layer);

			if (!(layer instanceof RenderLayer.MultiPhase)
					|| ((RenderLayer.MultiPhase) layer).getPhases().outlineMode == RenderLayer.OutlineMode.NONE) {
				return parentBuffer;
			}

			VertexConsumer plainBuffer = this.plainDrawer.getBuffer(
					layerCreator.apply(((RenderLayer.MultiPhase) layer).getPhases().texture));
			ColorVertexConsumer outlineVertexConsumer = new ColorVertexConsumer(plainBuffer, red, green, blue, alpha);
			return VertexConsumers.union(outlineVertexConsumer, parentBuffer);
		};
	}
	
	public VertexConsumerProvider createSingleProvider(VertexConsumerProvider parent, int red, int green, int blue, int alpha) {
		return layer -> {
			VertexConsumer parentBuffer = parent.getBuffer(layer);

			if (!(layer instanceof RenderLayer.MultiPhase)
					|| ((RenderLayer.MultiPhase) layer).getPhases().outlineMode == RenderLayer.OutlineMode.NONE) {
				return parentBuffer;
			}

			VertexConsumer plainBuffer = this.plainDrawer.getBuffer(
					layerCreator.apply(((RenderLayer.MultiPhase) layer).getPhases().texture));
			return new ColorVertexConsumer(plainBuffer, red, green, blue, alpha);
		};
	}

	public void setFramebuffer(Framebuffer framebuffer) {
		layerCreator = memoizeTexture(texture -> new RenderLayer(
				"bleachhack_outline", VertexFormats.POSITION_COLOR_TEXTURE, VertexFormat.DrawMode.QUADS, 256, false, false,
				() -> {
					texture.startDrawing();
					RenderSystem.setShader(shader);
					framebuffer.beginWrite(false);
				},
				() -> MinecraftClient.getInstance().getFramebuffer().beginWrite(false)) {});
	}

	private Function<TextureBase, RenderLayer> memoizeTexture(Function<TextureBase, RenderLayer> function) {
		return new Function<TextureBase, RenderLayer>() {
			private final Map<Identifier, RenderLayer> cache = new HashMap<>();

			public RenderLayer apply(TextureBase texture) {
				return this.cache.computeIfAbsent(texture.getId().get(), id -> function.apply(texture));
			}
		};
	}

	public void draw() {
		this.plainDrawer.draw();
	}

	static class ColorVertexConsumer extends FixedColorVertexConsumer {
		private final VertexConsumer delegate; // plainBuffer
		private double x;
		private double y;
		private double z;
		private float u;
		private float v;

		ColorVertexConsumer(VertexConsumer vertexConsumer, int i, int j, int k, int l) {
			this.delegate = vertexConsumer;
			super.fixedColor(i, j, k, l);
		}

		public void fixedColor(int red, int green, int blue, int alpha) {
		}

		public void unfixColor() {
		}

		public VertexConsumer vertex(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
			return this;
		}

		public VertexConsumer color(int red, int green, int blue, int alpha) {
			return this;
		}

		public VertexConsumer texture(float u, float v) {
			this.u = u;
			this.v = v;
			return this;
		}

		public VertexConsumer overlay(int u, int v) {
			return this;
		}

		public VertexConsumer light(int u, int v) {
			return this;
		}

		public VertexConsumer normal(float x, float y, float z) {
			return this;
		}

		public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v, int overlay, int light, float normalX, float normalY, float normalZ) {
			this.delegate.vertex((double)x, (double)y, (double)z).color(this.fixedRed, this.fixedGreen, this.fixedBlue, this.fixedAlpha).texture(u, v).next();
		}

		public void next() {
			this.delegate.vertex(this.x, this.y, this.z).color(this.fixedRed, this.fixedGreen, this.fixedBlue, this.fixedAlpha).texture(this.u, this.v).next();
		}
	}
}
