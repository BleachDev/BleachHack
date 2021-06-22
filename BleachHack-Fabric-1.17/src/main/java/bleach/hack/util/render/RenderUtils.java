/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util.render;

import org.apache.commons.lang3.ArrayUtils;

import com.mojang.blaze3d.systems.RenderSystem;

import bleach.hack.util.Boxes;
import bleach.hack.util.render.color.LineColor;
import bleach.hack.util.render.color.QuadColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public class RenderUtils {

	// -------------------- Fill + Outline Boxes --------------------

	public static void drawBoxBoth(BlockPos blockPos, QuadColor color, float lineWidth, Direction... excludeDirs) {
		drawBoxBoth(new Box(blockPos), color, lineWidth, excludeDirs);
	}

	public static void drawBoxBoth(Box box, QuadColor color, float lineWidth, Direction... excludeDirs) {
		QuadColor outlineColor = color.clone();
		outlineColor.overwriteAlpha(255);

		drawBoxBoth(box, color, outlineColor, lineWidth, excludeDirs);
	}

	public static void drawBoxBoth(BlockPos blockPos, QuadColor fillColor, QuadColor outlineColor, float lineWidth, Direction... excludeDirs) {
		drawBoxBoth(new Box(blockPos), fillColor, outlineColor, lineWidth, excludeDirs);
	}

	public static void drawBoxBoth(Box box, QuadColor fillColor, QuadColor outlineColor, float lineWidth, Direction... excludeDirs) {
		drawBoxFill(box, fillColor, excludeDirs);
		drawBoxOutline(box, outlineColor, lineWidth, excludeDirs);
	}

	// -------------------- Fill Boxes --------------------

	public static void drawBoxFill(BlockPos blockPos, QuadColor color, Direction... excludeDirs) {
		drawBoxFill(new Box(blockPos), color, excludeDirs);
	}

	public static void drawBoxFill(Box box, QuadColor color, Direction... excludeDirs) {
		setup();

		MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		// Fill
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		Vertexer.vertexBoxQuads(matrices, buffer, Boxes.moveToZero(box), color, excludeDirs);
		tessellator.draw();

		cleanup();
	}

	// -------------------- Outline Boxes --------------------

	public static void drawBoxOutline(BlockPos blockPos, QuadColor color, float lineWidth, Direction... excludeDirs) {
		drawBoxOutline(new Box(blockPos), color, lineWidth, excludeDirs);
	}

	public static void drawBoxOutline(Box box, QuadColor color, float lineWidth, Direction... excludeDirs) {
		setup();

		MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		// Outline
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
		RenderSystem.lineWidth(lineWidth);

		buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
		Vertexer.vertexBoxLines(matrices, buffer, Boxes.moveToZero(box), color, excludeDirs);
		tessellator.draw();

		RenderSystem.enableCull();

		cleanup();
	}

	// -------------------- Quads --------------------

	public static void drawQuadFill(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, int cullMode, QuadColor color) {
		setup();

		MatrixStack matrices = matrixFrom(x1, y1, z1);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		// Fill
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		Vertexer.vertexQuad(matrices, buffer,
				0f, 0f, 0f,
				(float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1),
				(float) (x3 - x1), (float) (y3 - y1), (float) (z3 - z1),
				(float) (x4 - x1), (float) (y4 - y1), (float) (z4 - z1),
				cullMode, color);
		tessellator.draw();

		cleanup();
	}

	public static void drawQuadOutline(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, float lineWidth, QuadColor color) {
		setup();

		MatrixStack matrices = matrixFrom(x1, y1, z1);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		int[] colors = color.getAllColors();

		// Outline
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
		RenderSystem.lineWidth(lineWidth);

		buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
		Vertexer.vertexLine(matrices, buffer, 0f, 0f, 0f, (float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1), LineColor.gradient(colors[0], colors[1]));
		Vertexer.vertexLine(matrices, buffer, (float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1), (float) (x3 - x1), (float) (y3 - y1), (float) (z3 - z1), LineColor.gradient(colors[1], colors[2]));
		Vertexer.vertexLine(matrices, buffer, (float) (x3 - x1), (float) (y3 - y1), (float) (z3 - z1), (float) (x4 - x1), (float) (y4 - y1), (float) (z4 - z1), LineColor.gradient(colors[2], colors[3]));
		Vertexer.vertexLine(matrices, buffer, (float) (x4 - x1), (float) (y4 - y1), (float) (z4 - z1), 0f, 0f, 0f, LineColor.gradient(colors[3], colors[0]));
		tessellator.draw();

		RenderSystem.enableCull();
		cleanup();
	}

	// -------------------- Lines --------------------

	public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, LineColor color, float width) {
		setup();

		MatrixStack matrices = matrixFrom(x1, y1, z1);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		// Line
		RenderSystem.disableDepthTest();
		RenderSystem.disableCull();
		RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
		RenderSystem.lineWidth(width);

		buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
		Vertexer.vertexLine(matrices, buffer, 0f, 0f, 0f, (float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1), color);
		tessellator.draw();

		RenderSystem.enableCull();
		RenderSystem.enableDepthTest();
		cleanup();
	}

	// -------------------- Utils --------------------

	public static MatrixStack matrixFrom(double x, double y, double z) {
		MatrixStack matrices = new MatrixStack();

		Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(camera.getPitch()));
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw() + 180.0F));

		matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);

		return matrices;
	}

	public static Vec3d getInterpolationOffset(Entity e) {
		if (MinecraftClient.getInstance().isPaused()) {
			return Vec3d.ZERO;
		}

		double tickDelta = (double) MinecraftClient.getInstance().getTickDelta();
		return new Vec3d(
				e.getX() - MathHelper.lerp(tickDelta, e.lastRenderX, e.getX()),
				e.getY() - MathHelper.lerp(tickDelta, e.lastRenderY, e.getY()),
				e.getZ() - MathHelper.lerp(tickDelta, e.lastRenderZ, e.getZ()));
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

		public static final int CULL_BACK = 0;
		public static final int CULL_FRONT = 1;
		public static final int CULL_NONE = 2;

		public static void vertexBoxQuads(MatrixStack matrices, VertexConsumer vertexConsumer, Box box, QuadColor quadColor, Direction... excludeDirs) {
			float x1 = (float) box.minX;
			float y1 = (float) box.minY;
			float z1 = (float) box.minZ;
			float x2 = (float) box.maxX;
			float y2 = (float) box.maxY;
			float z2 = (float) box.maxZ;

			int cullMode = excludeDirs.length == 0 ? CULL_BACK : CULL_NONE;

			if (!ArrayUtils.contains(excludeDirs, Direction.DOWN)) {
				vertexQuad(matrices, vertexConsumer, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, cullMode, quadColor);
			}

			if (!ArrayUtils.contains(excludeDirs, Direction.WEST)) {
				vertexQuad(matrices, vertexConsumer, x1, y1, z2, x1, y2, z2, x1, y2, z1, x1, y1, z1, cullMode, quadColor);
			}

			if (!ArrayUtils.contains(excludeDirs, Direction.EAST)) {
				vertexQuad(matrices, vertexConsumer, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, cullMode, quadColor);
			}

			if (!ArrayUtils.contains(excludeDirs, Direction.NORTH)) {
				vertexQuad(matrices, vertexConsumer, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, cullMode, quadColor);
			}

			if (!ArrayUtils.contains(excludeDirs, Direction.SOUTH)) {
				vertexQuad(matrices, vertexConsumer, x2, y1, z2, x2, y2, z2, x1, y2, z2, x1, y1, z2, cullMode, quadColor);
			}

			if (!ArrayUtils.contains(excludeDirs, Direction.UP)) {
				vertexQuad(matrices, vertexConsumer, x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, cullMode, quadColor);
			}
		}

		public static void vertexQuad(MatrixStack matrices, VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, int cullMode, QuadColor quadColor) {
			int[] color = quadColor.getAllColors();

			if (cullMode != CULL_FRONT) {
				vertexConsumer.vertex(matrices.peek().getModel(), x1, y1, z1).color(color[0], color[1], color[2], color[3]).next();
				vertexConsumer.vertex(matrices.peek().getModel(), x2, y2, z2).color(color[4], color[5], color[6], color[7]).next();
				vertexConsumer.vertex(matrices.peek().getModel(), x3, y3, z3).color(color[8], color[9], color[10], color[11]).next();
				vertexConsumer.vertex(matrices.peek().getModel(), x4, y4, z4).color(color[12], color[13], color[14], color[15]).next();
			}

			if (cullMode != CULL_BACK) {
				vertexConsumer.vertex(matrices.peek().getModel(), x4, y4, z4).color(color[0], color[1], color[2], color[3]).next();
				vertexConsumer.vertex(matrices.peek().getModel(), x3, y3, z3).color(color[4], color[5], color[6], color[7]).next();
				vertexConsumer.vertex(matrices.peek().getModel(), x2, y2, z2).color(color[8], color[9], color[10], color[11]).next();
				vertexConsumer.vertex(matrices.peek().getModel(), x1, y1, z1).color(color[12], color[13], color[14], color[15]).next();
			}
		}

		public static void vertexBoxLines(MatrixStack matrices, VertexConsumer vertexConsumer, Box box, QuadColor quadColor, Direction... excludeDirs) {
			float x1 = (float) box.minX;
			float y1 = (float) box.minY;
			float z1 = (float) box.minZ;
			float x2 = (float) box.maxX;
			float y2 = (float) box.maxY;
			float z2 = (float) box.maxZ;

			boolean exDown = ArrayUtils.contains(excludeDirs, Direction.DOWN);
			boolean exWest = ArrayUtils.contains(excludeDirs, Direction.WEST);
			boolean exEast = ArrayUtils.contains(excludeDirs, Direction.EAST);
			boolean exNorth = ArrayUtils.contains(excludeDirs, Direction.NORTH);
			boolean exSouth = ArrayUtils.contains(excludeDirs, Direction.SOUTH);
			boolean exUp = ArrayUtils.contains(excludeDirs, Direction.UP);

			int[] color = quadColor.getAllColors();

			if (!exDown) {
				vertexLine(matrices, vertexConsumer, x1, y1, z1, x2, y1, z1, LineColor.single(color[0], color[1], color[2], color[3]));
				vertexLine(matrices, vertexConsumer, x2, y1, z1, x2, y1, z2, LineColor.single(color[4], color[5], color[6], color[7]));
				vertexLine(matrices, vertexConsumer, x2, y1, z2, x1, y1, z2, LineColor.single(color[8], color[9], color[10], color[11]));
				vertexLine(matrices, vertexConsumer, x1, y1, z2, x1, y1, z1, LineColor.single(color[12], color[13], color[14], color[15]));
			}

			if (!exWest) {
				if (exDown) vertexLine(matrices, vertexConsumer, x1, y1, z1, x1, y1, z2, LineColor.single(color[0], color[1], color[2], color[3]));
				vertexLine(matrices, vertexConsumer, x1, y1, z2, x1, y2, z2, LineColor.single(color[4], color[5], color[6], color[7]));
				vertexLine(matrices, vertexConsumer, x1, y1, z1, x1, y2, z1, LineColor.single(color[8], color[9], color[10], color[11]));
				if (exUp) vertexLine(matrices, vertexConsumer, x1, y2, z1, x1, y2, z2, LineColor.single(color[12], color[13], color[14], color[15]));
			}

			if (!exEast) {
				if (exDown) vertexLine(matrices, vertexConsumer, x2, y1, z1, x2, y1, z2, LineColor.single(color[0], color[1], color[2], color[3]));
				vertexLine(matrices, vertexConsumer, x2, y1, z2, x2, y2, z2, LineColor.single(color[4], color[5], color[6], color[7]));
				vertexLine(matrices, vertexConsumer, x2, y1, z1, x2, y2, z1, LineColor.single(color[8], color[9], color[10], color[11]));
				if (exUp) vertexLine(matrices, vertexConsumer, x2, y2, z1, x2, y2, z2, LineColor.single(color[12], color[13], color[14], color[15]));
			}

			if (!exNorth) {
				if (exDown) vertexLine(matrices, vertexConsumer, x1, y1, z1, x2, y1, z1, LineColor.single(color[0], color[1], color[2], color[3]));
				if (exEast) vertexLine(matrices, vertexConsumer, x2, y1, z1, x2, y2, z1, LineColor.single(color[4], color[5], color[6], color[7]));
				if (exWest) vertexLine(matrices, vertexConsumer, x1, y1, z1, x1, y2, z1, LineColor.single(color[8], color[9], color[10], color[11]));
				if (exUp) vertexLine(matrices, vertexConsumer, x1, y2, z1, x2, y2, z1, LineColor.single(color[12], color[13], color[14], color[15]));
			}

			if (!exSouth) {
				if (exDown) vertexLine(matrices, vertexConsumer, x1, y1, z2, x2, y1, z2, LineColor.single(color[0], color[1], color[2], color[3]));
				if (exEast) vertexLine(matrices, vertexConsumer, x2, y1, z2, x2, y2, z2, LineColor.single(color[4], color[5], color[6], color[7]));
				if (exWest) vertexLine(matrices, vertexConsumer, x1, y1, z2, x1, y2, z2, LineColor.single(color[8], color[9], color[10], color[11]));
				if (exUp) vertexLine(matrices, vertexConsumer, x1, y2, z2, x2, y2, z2, LineColor.single(color[12], color[13], color[14], color[15]));
			}

			if (!exUp) {
				vertexLine(matrices, vertexConsumer, x1, y2, z1, x2, y2, z1, LineColor.single(color[0], color[1], color[2], color[3]));
				vertexLine(matrices, vertexConsumer, x2, y2, z1, x2, y2, z2, LineColor.single(color[4], color[5], color[6], color[7]));
				vertexLine(matrices, vertexConsumer, x2, y2, z2, x1, y2, z2, LineColor.single(color[8], color[9], color[10], color[11]));
				vertexLine(matrices, vertexConsumer, x1, y2, z2, x1, y2, z1, LineColor.single(color[12], color[13], color[14], color[15]));
			}
		}

		public static void vertexLine(MatrixStack matrices, VertexConsumer vertexConsumer, float x1, float y1, float z1, float x2, float y2, float z2, LineColor lineColor) {
			Matrix4f model = matrices.peek().getModel();
			Matrix3f normal = matrices.peek().getNormal();

			Vec3f normalVec = getNormal(normal, x1, y1, z1, x2, y2, z2);

			int[] color1 = lineColor.getColor(x1, y1, z1, 0);
			int[] color2 = lineColor.getColor(x2, y2, z2, 1);

			vertexConsumer.vertex(model, x1, y1, z1).color(color1[0], color1[1], color1[2], color1[3]).normal(normal, normalVec.getX(), normalVec.getY(), normalVec.getZ()).next();
			vertexConsumer.vertex(model, x2, y2, z2).color(color2[0], color2[1], color2[2], color2[3]).normal(normal, normalVec.getX(), normalVec.getY(), normalVec.getZ()).next();
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
