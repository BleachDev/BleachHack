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
package bleach.hack.util;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

public class RenderUtils {

	public static void drawFilledBox(BlockPos blockPos, float r, float g, float b, float a) {
		drawFilledBox(new Box(blockPos), r, g, b, a);
	}

	public static void drawFilledBox(Box box, float r, float g, float b, float a) {
		drawFilledBox(box, r, g, b, a, 2.5f);
	}

	public static void drawFilledBox(Box box, float r, float g, float b, float a, float width) {
		setup();
		
		MatrixStack matrix = matrixFromOrigin();
		Matrix4f model = matrix.peek().getModel();

		// Fill
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
		WorldRenderer.drawBox(matrix, buffer, box, r, g, b, a / 2f);
		tessellator.draw();

		// Outline
		RenderSystem.lineWidth(width);
		buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
		buffer.vertex(model, (float) box.minX, (float) box.minY, (float) box.minZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.minX, (float) box.minY, (float) box.maxZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.maxX, (float) box.minY, (float) box.minZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.minX, (float) box.minY, (float) box.minZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.minX, (float) box.maxY, (float) box.minZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.minX, (float) box.maxY, (float) box.minZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.minX, (float) box.minY, (float) box.maxZ).color(r, g, b, 0f).next();
		buffer.vertex(model, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(r, g, b, 0f).next();
		buffer.vertex(model, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.maxX, (float) box.minY, (float) box.minZ).color(r, g, b, 0f).next();
		buffer.vertex(model, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(r, g, b, a).next();
		tessellator.draw();

		cleanup();
	}
	
	public static void drawFill(BlockPos blockPos, float r, float g, float b, float a) {
		drawFill(new Box(blockPos), r, g, b, a);
	}

	public static void drawFill(Box box, float r, float g, float b, float a) {
		setup();
		
		MatrixStack matrix = matrixFromOrigin();

		// Fill
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
		WorldRenderer.drawBox(matrix, buffer, box, r, g, b, a / 2f);
		tessellator.draw();

		cleanup();
	}

	public static void drawOutline(BlockPos blockPos, float r, float g, float b, float a) {
		drawOutline(new Box(blockPos), r, g, b, a);
	}
	
	public static void drawOutline(Box box, float r, float g, float b, float a) {
		drawOutline(box, r, g, b, a, 2.5f);
	}

	public static void drawOutline(Box box, float r, float g, float b, float a, float width) {
		setup();
		
		MatrixStack matrix = matrixFromOrigin();
		Matrix4f model = matrix.peek().getModel();

		RenderSystem.lineWidth(width);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		// Outline
		buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
		buffer.vertex(model, (float) box.minX, (float) box.minY, (float) box.minZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.minX, (float) box.minY, (float) box.maxZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.maxX, (float) box.minY, (float) box.minZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.minX, (float) box.minY, (float) box.minZ).color(r, g, b, a).next();
		
		buffer.vertex(model, (float) box.minX, (float) box.maxY, (float) box.minZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.minX, (float) box.maxY, (float) box.minZ).color(r, g, b, a).next();
		tessellator.draw();
		
		buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
		buffer.vertex(model, (float) box.maxX, (float) box.minY, (float) box.minZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(r, g, b, a).next();
		tessellator.draw();
		
		buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
		buffer.vertex(model, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(r, g, b, a).next();
		tessellator.draw();
		
		buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
		buffer.vertex(model, (float) box.minX, (float) box.minY, (float) box.maxZ).color(r, g, b, a).next();
		buffer.vertex(model, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(r, g, b, a).next();
		tessellator.draw();

		cleanup();
	}

	public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float t) {
		setup();
		
		MatrixStack matrix = matrixFromOrigin();
		Matrix4f model = matrix.peek().getModel();
		
		RenderSystem.lineWidth(t);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
		buffer.vertex(model, (float) x1, (float) y1, (float) z1).color(r, g, b, 0.0F).next();
		buffer.vertex(model, (float) x1, (float) y1, (float) z1).color(r, g, b, 1.0F).next();
		buffer.vertex(model, (float) x2, (float) y2, (float) z2).color(r, g, b, 1.0F).next();
		tessellator.draw();

		cleanup();
	}
	
	public static MatrixStack matrixFromOrigin() {
		MatrixStack matrix = new MatrixStack();

		Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		matrix.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
		matrix.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));

		matrix.translate(-camera.getPos().x, -camera.getPos().y, -camera.getPos().z);

		return matrix;
	}

	/*public static void offsetRender() {
		Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		Vec3d camPos = camera.getPos();
		RenderSystem.rotatef(MathHelper.wrapDegrees(camera.getPitch()), 1f, 0f, 0f);
		RenderSystem.rotatef(MathHelper.wrapDegrees(camera.getYaw() + 180.0f), 0f, 1f, 0f);
		RenderSystem.translated(-camPos.x, -camPos.y, -camPos.z);
	}*/

	public static void setup() {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.lineWidth(2.5f);
		RenderSystem.disableTexture();
	}

	public static void cleanup() {
		RenderSystem.enableTexture();
		RenderSystem.enableBlend();
	}
}
