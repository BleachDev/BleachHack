/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
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
package bleach.hack.utils;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemStack;

public class WorldRenderUtils {

	private static MinecraftClient mc = MinecraftClient.getInstance();

	/**
	 * Draws a Text string in the world.
	 *  
	 * @return The used MatrixStack for further use
	 */
	public static MatrixStack drawText(String str, double x, double y, double z, double scale) {
		MatrixStack matrix = matrixFrom(x, y, z);
		
		Camera camera = mc.gameRenderer.getCamera();
	    matrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-camera.getYaw()));
	    matrix.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
		
	    GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	    GL11.glDepthFunc(GL11.GL_ALWAYS);
		
	    matrix.scale(-0.025f * (float) scale, -0.025f * (float) scale, 1);

		int i = mc.textRenderer.getWidth(str) / 2;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, VertexFormats.POSITION_COLOR);
		float f = mc.options.getTextBackgroundOpacity(0.3F);
		bufferbuilder.vertex(matrix.peek().getModel(), -i - 1, -1, 0.0f).color(0.0F, 0.0F, 0.0F, f).next();
		bufferbuilder.vertex(matrix.peek().getModel(), -i - 1, 8, 0.0f).color(0.0F, 0.0F, 0.0F, f).next();
		bufferbuilder.vertex(matrix.peek().getModel(), i + 1, 8, 0.0f).color(0.0F, 0.0F, 0.0F, f).next();
		bufferbuilder.vertex(matrix.peek().getModel(), i + 1, -1, 0.0f).color(0.0F, 0.0F, 0.0F, f).next();
		tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		mc.textRenderer.draw(matrix, str, -i, 0, 553648127);
		mc.textRenderer.draw(matrix, str, -i, 0, -1);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		
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
	    matrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-camera.getYaw()));
	    matrix.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
	    
	    matrix.scale((float) scale, (float) scale, 0.001f);
	    matrix.translate(offX, offY, 0);
	    
	    if (item.isEmpty())
			return matrix;
	    
	    matrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180f));
		
		mc.getBufferBuilders().getEntityVertexConsumers().draw();
		
		DiffuseLighting.disableGuiDepthLighting();
		GL11.glDepthFunc(GL11.GL_ALWAYS);
		mc.getItemRenderer().renderItem(item, ModelTransformation.Mode.GUI, 0xF000F0,
				OverlayTexture.DEFAULT_UV, matrix, mc.getBufferBuilders().getEntityVertexConsumers());
		
		mc.getBufferBuilders().getEntityVertexConsumers().draw();
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		
		matrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(-180f));
		
		return matrix;
	}
	
	private static MatrixStack matrixFrom(double x, double y, double z) {
		MatrixStack matrix = new MatrixStack();
		
		Camera camera = mc.gameRenderer.getCamera();
		matrix.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
	    matrix.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));

		matrix.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);
		
		return matrix;
	}
}
