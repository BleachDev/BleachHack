/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.render;

import org.apache.commons.lang3.reflect.FieldUtils;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3f;

public class WorldRenderer {

	private static final MinecraftClient mc = MinecraftClient.getInstance();

	// A Pointer to RenderSystem.shaderLightDirections
	private static final Vec3f[] shaderLight;

	static {
		try {
			shaderLight = (Vec3f[]) FieldUtils.getField(RenderSystem.class, "shaderLightDirections", true).get(null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/** Draws text in the world. **/
	public static void drawText(Text text, double x, double y, double z, double scale, boolean shadow) {
		drawText(text, x, y, z, 0, 0, scale, shadow);
	}

	/** Draws text in the world. **/
	public static void drawText(Text text, double x, double y, double z, double offX, double offY, double scale, boolean fill) {
		MatrixStack matrices = matrixFrom(x, y, z);

		Camera camera = mc.gameRenderer.getCamera();
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-camera.getYaw()));
		matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		matrices.translate(offX, offY, 0);
		matrices.scale(-0.025f * (float) scale, -0.025f * (float) scale, 1);

		int halfWidth = mc.textRenderer.getWidth(text) / 2;

		VertexConsumerProvider.Immediate immediate = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

		if (fill) {
			int opacity = (int) (MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F) * 255.0F) << 24;
			mc.textRenderer.draw(text, -halfWidth, 0f, 553648127, false, matrices.peek().getPositionMatrix(), immediate, true, opacity, 0xf000f0);
			immediate.draw();
		} else {
			matrices.push();
			matrices.translate(1, 1, 0);
			mc.textRenderer.draw(text.copy(), -halfWidth, 0f, 0x202020, false, matrices.peek().getPositionMatrix(), immediate, true, 0, 0xf000f0);
			immediate.draw();
			matrices.pop();
		}

		mc.textRenderer.draw(text, -halfWidth, 0f, -1, false, matrices.peek().getPositionMatrix(), immediate, true, 0, 0xf000f0);
		immediate.draw();

		RenderSystem.disableBlend();
	}

	/** Draws a 2D gui items somewhere in the world. **/
	public static void drawGuiItem(double x, double y, double z, double offX, double offY, double scale, ItemStack item) {
		if (item.isEmpty()) {
			return;
		}

		MatrixStack matrices = matrixFrom(x, y, z);

		Camera camera = mc.gameRenderer.getCamera();
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-camera.getYaw()));
		matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));

		matrices.translate(offX, offY, 0);
		matrices.scale((float) scale, (float) scale, 0.001f);

		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180f));

		mc.getBufferBuilders().getEntityVertexConsumers().draw();
		
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		Vec3f[] currentLight = shaderLight.clone();
		DiffuseLighting.disableGuiDepthLighting();

		mc.getItemRenderer().renderItem(item, ModelTransformation.Mode.GUI, 0xF000F0,
				OverlayTexture.DEFAULT_UV, matrices, mc.getBufferBuilders().getEntityVertexConsumers(), 0);

		mc.getBufferBuilders().getEntityVertexConsumers().draw();

		RenderSystem.setShaderLights(currentLight[0], currentLight[1]);
		RenderSystem.disableBlend();
	}

	public static MatrixStack matrixFrom(double x, double y, double z) {
		MatrixStack matrices = new MatrixStack();

		Camera camera = mc.gameRenderer.getCamera();
		matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));

		matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);

		return matrices;
	}
}
