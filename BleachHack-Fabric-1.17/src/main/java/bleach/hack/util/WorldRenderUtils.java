/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2019 Bleach.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.util;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3f;

public class WorldRenderUtils {

	private static final MinecraftClient mc = MinecraftClient.getInstance();

	/**
	 * Draws a Text string in the world.
	 *  
	 * @return The used MatrixStack for further use
	 */
	public static MatrixStack drawText(String str, double x, double y, double z, double scale) {
		MatrixStack matrix = matrixFrom(x, y, z);

		Camera camera = mc.gameRenderer.getCamera();
		matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-camera.getYaw()));
		matrix.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		matrix.scale(-0.025f * (float) scale, -0.025f * (float) scale, 1);
		
		int halfWidth = mc.textRenderer.getWidth(str) / 2;
		
        int opacity = (int) (MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F) * 255.0F) << 24;
		
		mc.textRenderer.draw(str, -halfWidth, 0f, 553648127, false, matrix.peek().getModel(), mc.getBufferBuilders().getEntityVertexConsumers(), true, opacity, 0xf000f0);
        mc.textRenderer.draw(str, -halfWidth, 0f, -1, false, matrix.peek().getModel(), mc.getBufferBuilders().getEntityVertexConsumers(), true, 0, 0xf000f0);

		RenderSystem.disableBlend();

		return matrix;
	}

	/**
	 * Draws a 2D gui items somewhere in the world.
	 *  
	 * @return The used MatrixStack for further use
	 */
	public static MatrixStack drawGuiItem(double x, double y, double z, double offX, double offY, double scale, ItemStack item) {
		MatrixStack matrix = matrixFrom(x, y, z);

		Camera camera = mc.gameRenderer.getCamera();
		matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-camera.getYaw()));
		matrix.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));

		matrix.scale((float) scale, (float) scale, 0.001f);
		matrix.translate(offX, offY, 0);

		if (item.isEmpty())
			return matrix;

		matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180f));

		mc.getBufferBuilders().getEntityVertexConsumers().draw();

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL11.GL_ALWAYS);

		//DiffuseLighting.disableGuiDepthLighting();

		mc.getBufferBuilders().getOutlineVertexConsumers().setColor(255, 255, 255, 255);
		mc.getItemRenderer().renderItem(item, ModelTransformation.Mode.GUI, 0xF000F0,
				OverlayTexture.DEFAULT_UV, matrix, mc.getBufferBuilders().getOutlineVertexConsumers() /* yeah fuck sure */, 0);

		mc.getBufferBuilders().getEntityVertexConsumers().draw();

		//DiffuseLighting.enableGuiDepthLighting();

		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		RenderSystem.disableDepthTest();

		RenderSystem.disableBlend();

		matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-180f));

		return matrix;
	}

	public static MatrixStack matrixFrom(double x, double y, double z) {
		MatrixStack matrix = new MatrixStack();

		Camera camera = mc.gameRenderer.getCamera();
		matrix.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
		matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));

		matrix.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);

		return matrix;
	}
}
