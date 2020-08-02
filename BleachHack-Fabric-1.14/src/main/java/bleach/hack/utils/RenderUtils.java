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
import org.lwjgl.opengl.GL14;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class RenderUtils {

	private static MinecraftClient mc = MinecraftClient.getInstance();

	public static void drawFilledBox(BlockPos blockPos, float r, float g, float b, float a) {
		drawFilledBox(new Box(
				blockPos.getX(), blockPos.getY(), blockPos.getZ(),
				blockPos.getX()+1, blockPos.getY()+1, blockPos.getZ()+1), r, g, b, a);
	}

	public static void drawFilledBox(Box box, float r, float g, float b, float a) {
		gl11Setup();

		Vec3d ren = renderPos();

		/* Fill */
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBufferBuilder();
		buffer.begin(5, VertexFormats.POSITION_COLOR);
		WorldRenderer.buildBox(buffer,
				box.minX - ren.x, box.minY - ren.y, box.minZ - ren.z,
				box.maxX - ren.x, box.maxY - ren.y, box.maxZ - ren.z, r, g, b, a/2f);
		tessellator.draw();

		/* Outline */
		WorldRenderer.drawBoxOutline(new Box(
				box.minX - ren.x, box.minY - ren.y, box.minZ - ren.z,
				box.maxX - ren.x, box.maxY - ren.y, box.maxZ - ren.z), r, g, b, a);

		gl11Cleanup();
	}

	public static void drawLine(double x1,double y1,double z1,double x2,double y2,double z2, float r, float g, float b, float t) {
		gl11Setup();
		GL11.glLineWidth(t);

		Vec3d ren = renderPos();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBufferBuilder();
		buffer.begin(3, VertexFormats.POSITION_COLOR);
		buffer.vertex(x1 - ren.x, y1 - ren.y, z1 - ren.z).color(r, g, b, 0.0F).next();
		buffer.vertex(x1 - ren.x, y1 - ren.y, z1 - ren.z).color(r, g, b, 1.0F).next();
		buffer.vertex(x2 - ren.x, y2 - ren.y, z2 - ren.z).color(r, g, b, 1.0F).next();
		tessellator.draw();

		gl11Cleanup();

	}

	public static Vec3d renderPos() {
		return mc.gameRenderer.getCamera().getPos();
	}

	public static void gl11Setup() {
		GL11.glEnable(GL11.GL_BLEND);
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glLineWidth(2.5F);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glMatrixMode(5889);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glPushMatrix();
	}

	public static void gl11Cleanup() {
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		GL11.glPopMatrix();
		GL11.glMatrixMode(5888);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
