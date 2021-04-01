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
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

public class RenderUtils {

	public static void drawFilledBox(BlockPos blockPos, float red, float green, float blue, float alpha) {
		drawFilledBox(new Box(blockPos), red, green, blue, alpha);
	}

	public static void drawFilledBox(Box box, float red, float green, float blue, float alpha) {
		drawFilledBox(box, red, green, blue, alpha, 2.5f);
	}

	public static void drawFilledBox(Box box, float red, float green, float blue, float alpha, float width) {
		setup();

		MatrixStack matrix = matrixFromOrigin();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		// Fill
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		Vertexer.vertexBoxQuads(matrix, buffer, box, red, green, blue, alpha);
		tessellator.draw();

		// Outline
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
		RenderSystem.lineWidth(width);

		buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
		Vertexer.vertexBoxLines(matrix, buffer, box, red, green, blue, alpha);
		tessellator.draw();

		RenderSystem.enableCull();
		cleanup();
	}

	public static void drawFill(BlockPos blockPos, float red, float green, float blue, float alpha) {
		drawFill(new Box(blockPos), red, green, blue, alpha);
	}

	public static void drawFill(Box box, float red, float green, float blue, float alpha) {
		setup();

		MatrixStack matrix = matrixFromOrigin();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		// Fill
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		Vertexer.vertexBoxQuads(matrix, buffer, box, red, green, blue, alpha);
		tessellator.draw();

		cleanup();
	}

	public static void drawOutline(BlockPos blockPos, float red, float green, float blue, float alpha, float width) {
		drawOutline(new Box(blockPos), red, green, blue, alpha, width);
	}

	public static void drawOutline(Box box, float red, float green, float blue, float alpha, float width) {
		setup();

		MatrixStack matrix = matrixFromOrigin();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		// Outline
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
		RenderSystem.lineWidth(width);

		buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
		Vertexer.vertexBoxLines(matrix, buffer, box, red, green, blue, alpha);
		tessellator.draw();

		RenderSystem.enableCull();
		cleanup();
	}

	public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, float red, float green, float blue, float width) {
		setup();

		MatrixStack matrix = matrixFromOrigin();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		// Line
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
		RenderSystem.lineWidth(width);

		buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
		Vertexer.vertexLine(matrix, buffer, (float) x1, (float) y1, (float) z1, (float) x2, (float) y2, (float) z2, red, green, blue, 0.5f);
		tessellator.draw();

		RenderSystem.enableCull();
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

	public static void setup() {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableTexture();
	}

	public static void cleanup() {
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}

	public static class Vertexer {

		public static void vertexBoxQuads(MatrixStack matrix, VertexConsumer vertexConsumer, Box box, float red, float green, float blue, float alpha) {
			float x1 = (float) box.minX;
			float y1 = (float) box.minY;
			float z1 = (float) box.minZ;
			float x2 = (float) box.maxX;
			float y2 = (float) box.maxY;
			float z2 = (float) box.maxZ;

			// Bottom
			vertexConsumer.vertex(matrix.peek().getModel(), x1, y1, z1).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x2, y1, z1).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x2, y1, z2).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x1, y1, z2).color(red, green, blue, alpha).next();

			// X-
			vertexConsumer.vertex(matrix.peek().getModel(), x1, y1, z2).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x1, y2, z2).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x1, y2, z1).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x1, y1, z1).color(red, green, blue, alpha).next();

			// X+
			vertexConsumer.vertex(matrix.peek().getModel(), x2, y1, z1).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x2, y2, z1).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x2, y2, z2).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x2, y1, z2).color(red, green, blue, alpha).next();

			// Z-
			vertexConsumer.vertex(matrix.peek().getModel(), x1, y1, z1).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x1, y2, z1).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x2, y2, z1).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x2, y1, z1).color(red, green, blue, alpha).next();

			// Z+
			vertexConsumer.vertex(matrix.peek().getModel(), x2, y1, z2).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x2, y2, z2).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x1, y2, z2).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x1, y1, z2).color(red, green, blue, alpha).next();

			// Top
			vertexConsumer.vertex(matrix.peek().getModel(), x1, y2, z2).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x2, y2, z2).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x2, y2, z1).color(red, green, blue, alpha).next();
			vertexConsumer.vertex(matrix.peek().getModel(), x1, y2, z1).color(red, green, blue, alpha).next();
		}

		public static void vertexBoxLines(MatrixStack matrix, VertexConsumer vertexConsumer, Box box, float red, float green, float blue, float alpha) {
			float x1 = (float) box.minX;
			float y1 = (float) box.minY;
			float z1 = (float) box.minZ;
			float x2 = (float) box.maxX;
			float y2 = (float) box.maxY;
			float z2 = (float) box.maxZ;

			// Bottom
			vertexLine(matrix, vertexConsumer, x1, y1, z1, x2, y1, z1, red, green, blue, alpha);
			vertexLine(matrix, vertexConsumer, x2, y1, z1, x2, y1, z2, red, green, blue, alpha);
			vertexLine(matrix, vertexConsumer, x2, y1, z2, x1, y1, z2, red, green, blue, alpha);
			vertexLine(matrix, vertexConsumer, x1, y1, z2, x1, y1, z1, red, green, blue, alpha);

			// Pillars
			vertexLine(matrix, vertexConsumer, x1, y1, z1, x1, y2, z1, red, green, blue, alpha);
			vertexLine(matrix, vertexConsumer, x2, y1, z1, x2, y2, z1, red, green, blue, alpha);
			vertexLine(matrix, vertexConsumer, x2, y1, z2, x2, y2, z2, red, green, blue, alpha);
			vertexLine(matrix, vertexConsumer, x1, y1, z2, x1, y2, z2, red, green, blue, alpha);

			// Top
			vertexLine(matrix, vertexConsumer, x1, y2, z1, x2, y2, z1, red, green, blue, alpha);
			vertexLine(matrix, vertexConsumer, x2, y2, z1, x2, y2, z2, red, green, blue, alpha);
			vertexLine(matrix, vertexConsumer, x2, y2, z2, x1, y2, z2, red, green, blue, alpha);
			vertexLine(matrix, vertexConsumer, x1, y2, z2, x1, y2, z1, red, green, blue, alpha);
		}

		public static void vertexLine(MatrixStack matrix, VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float y2, float z2, float red, float green, float blue, float alpha) {
			Matrix4f model = matrix.peek().getModel();
			Matrix3f normal = matrix.peek().getNormal();

			Vec3f normalVec = getNormal(normal, x1, y1, z1, x2, y2, z2);

			vertexConsumer.vertex(model, x1, y1, z1).color(red, green, blue, alpha).normal(normal, normalVec.getX(), normalVec.getY(), normalVec.getZ()).next();
			vertexConsumer.vertex(model, x2, y2, z2).color(red, green, blue, alpha).normal(normal, normalVec.getX(), normalVec.getY(), normalVec.getZ()).next();
		}

		public static Vec3f getNormal(Matrix3f normal, float x1, float y1, float z1, float x2, float y2, float z2) {
			float xNormal = x2 - x1;
			float yNormal = y2 - y1;
			float zNormal = z2 - z1;
			float normalSqrt = MathHelper.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal);

			return new Vec3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
		}
	}
}
