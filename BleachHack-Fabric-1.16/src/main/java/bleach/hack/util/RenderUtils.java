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

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RenderUtils {

	public static void drawFilledBox(BlockPos blockPos, float r, float g, float b, float a) {
		drawFilledBox(new Box(blockPos), r, g, b, a);
	}

	public static void drawFilledBox(Box box, float r, float g, float b, float a) {
		drawFilledBox(box, r, g, b, a, 2.5f);
	}

	public static void drawFilledBox(Box box, float r, float g, float b, float a, float width) {
		setup();

		// Fill
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(5, VertexFormats.POSITION_COLOR);
		WorldRenderer.drawBox(buffer,
				box.minX, box.minY, box.minZ,
				box.maxX, box.maxY, box.maxZ, r, g, b, a / 2f);
		tessellator.draw();

		// Outline
		RenderSystem.lineWidth(width);
		buffer.begin(3, VertexFormats.POSITION_COLOR);
		buffer.vertex(box.minX, box.minY, box.minZ).color(r, g, b, a).next();
		buffer.vertex(box.minX, box.minY, box.maxZ).color(r, g, b, a).next();
		buffer.vertex(box.maxX, box.minY, box.maxZ).color(r, g, b, a).next();
		buffer.vertex(box.maxX, box.minY, box.minZ).color(r, g, b, a).next();
		buffer.vertex(box.minX, box.minY, box.minZ).color(r, g, b, a).next();
		buffer.vertex(box.minX, box.maxY, box.minZ).color(r, g, b, a).next();
		buffer.vertex(box.maxX, box.maxY, box.minZ).color(r, g, b, a).next();
		buffer.vertex(box.maxX, box.maxY, box.maxZ).color(r, g, b, a).next();
		buffer.vertex(box.minX, box.maxY, box.maxZ).color(r, g, b, a).next();
		buffer.vertex(box.minX, box.maxY, box.minZ).color(r, g, b, a).next();
		buffer.vertex(box.minX, box.minY, box.maxZ).color(r, g, b, 0f).next();
		buffer.vertex(box.minX, box.maxY, box.maxZ).color(r, g, b, a).next();
		buffer.vertex(box.maxX, box.minY, box.maxZ).color(r, g, b, 0f).next();
		buffer.vertex(box.maxX, box.maxY, box.maxZ).color(r, g, b, a).next();
		buffer.vertex(box.maxX, box.minY, box.minZ).color(r, g, b, 0f).next();
		buffer.vertex(box.maxX, box.maxY, box.minZ).color(r, g, b, a).next();
		tessellator.draw();

		cleanup();
	}
	
	public static void drawFill(BlockPos blockPos, float r, float g, float b, float a) {
		drawFill(new Box(blockPos), r, g, b, a);
	}

	public static void drawFill(Box box, float r, float g, float b, float a) {
		setup();

		// Fill
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(5, VertexFormats.POSITION_COLOR);
		WorldRenderer.drawBox(buffer,
				box.minX, box.minY, box.minZ,
				box.maxX, box.maxY, box.maxZ, r, g, b, a / 2f);
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

		RenderSystem.lineWidth(width);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		// Outline
		buffer.begin(3, VertexFormats.POSITION_COLOR);
		buffer.vertex(box.minX, box.minY, box.minZ).color(r, g, b, a).next();
		buffer.vertex(box.minX, box.minY, box.maxZ).color(r, g, b, a).next();
		buffer.vertex(box.maxX, box.minY, box.maxZ).color(r, g, b, a).next();
		buffer.vertex(box.maxX, box.minY, box.minZ).color(r, g, b, a).next();
		buffer.vertex(box.minX, box.minY, box.minZ).color(r, g, b, a).next();
		buffer.vertex(box.minX, box.maxY, box.minZ).color(r, g, b, a).next();
		buffer.vertex(box.maxX, box.maxY, box.minZ).color(r, g, b, a).next();
		buffer.vertex(box.maxX, box.maxY, box.maxZ).color(r, g, b, a).next();
		buffer.vertex(box.minX, box.maxY, box.maxZ).color(r, g, b, a).next();
		buffer.vertex(box.minX, box.maxY, box.minZ).color(r, g, b, a).next();
		buffer.vertex(box.minX, box.minY, box.maxZ).color(r, g, b, 0f).next();
		buffer.vertex(box.minX, box.maxY, box.maxZ).color(r, g, b, a).next();
		buffer.vertex(box.maxX, box.minY, box.maxZ).color(r, g, b, 0f).next();
		buffer.vertex(box.maxX, box.maxY, box.maxZ).color(r, g, b, a).next();
		buffer.vertex(box.maxX, box.minY, box.minZ).color(r, g, b, 0f).next();
		buffer.vertex(box.maxX, box.maxY, box.minZ).color(r, g, b, a).next();
		tessellator.draw();

		cleanup();
	}

	public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float t) {
		setup();
		RenderSystem.lineWidth(t);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(3, VertexFormats.POSITION_COLOR);
		buffer.vertex(x1, y1, z1).color(r, g, b, 0.0F).next();
		buffer.vertex(x1, y1, z1).color(r, g, b, 1.0F).next();
		buffer.vertex(x2, y2, z2).color(r, g, b, 1.0F).next();
		tessellator.draw();

		cleanup();
	}

	public static void offsetRender() {
		Camera camera = BlockEntityRenderDispatcher.INSTANCE.camera;
		Vec3d camPos = camera.getPos();
		RenderSystem.rotatef(MathHelper.wrapDegrees(camera.getPitch()), 1f, 0f, 0f);
		RenderSystem.rotatef(MathHelper.wrapDegrees(camera.getYaw() + 180.0f), 0f, 1f, 0f);
		RenderSystem.translated(-camPos.x, -camPos.y, -camPos.z);
	}

	public static void setup() {
		RenderSystem.pushMatrix();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.lineWidth(2.5f);
		RenderSystem.disableTexture();
		RenderSystem.enableLineOffset();
		//GL11.glEnable(GL11.GL_LINE_SMOOTH);
		offsetRender();
	}

	public static void cleanup() {
		//GL11.glDisable(GL11.GL_LINE_SMOOTH);
		RenderSystem.enableTexture();
		RenderSystem.enableBlend();
		RenderSystem.popMatrix();
	}
}
