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

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class RenderUtils {

    public static void drawFilledBox(BlockPos blockPos, float r, float g, float b, float a) {
        drawFilledBox(new Box(blockPos), r, g, b, a);
    }

    public static void drawFilledBox(Box box, float r, float g, float b, float a) {
        gl11Setup();

        // Fill
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(5, VertexFormats.POSITION_COLOR);
        WorldRenderer.drawBox(buffer,
                box.minX, box.minY, box.minZ,
                box.maxX, box.maxY, box.maxZ, r, g, b, a / 2f);
        tessellator.draw();

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

        gl11Cleanup();
    }

    public static void drawOutlineBox(BlockPos blockPos, float r, float g, float b, float a) {
        drawOutlineBox(new Box(blockPos), r, g, b, a);
    }

    public static void drawOutlineBox(Box box, float r, float g, float b, float a) {
        gl11Setup();

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

        gl11Cleanup();
    }

    public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float t) {
        gl11Setup();
        GL11.glLineWidth(t);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(3, VertexFormats.POSITION_COLOR);
        buffer.vertex(x1, y1, z1).color(r, g, b, 0.0F).next();
        buffer.vertex(x1, y1, z1).color(r, g, b, 1.0F).next();
        buffer.vertex(x2, y2, z2).color(r, g, b, 1.0F).next();
        tessellator.draw();

        gl11Cleanup();

    }

    public static void drawRect(float x, float y, float w, float h, int color, float alpha)
    {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture();
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        bufferbuilder.begin(7, VertexFormats.POSITION_COLOR);
        bufferbuilder.vertex((double) x, (double) h, 0.0D).color(red, green, blue, alpha).next();
        bufferbuilder.vertex((double) w, (double) h, 0.0D).color(red, green, blue, alpha).next();
        bufferbuilder.vertex((double) w, (double) y, 0.0D).color(red, green, blue, alpha).next();
        bufferbuilder.vertex((double) x, (double) y, 0.0D).color(red, green, blue, alpha).next();
        tessellator.draw();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
    }

    public static void offsetRender() {
        Camera camera = BlockEntityRenderDispatcher.INSTANCE.camera;
        Vec3d camPos = camera.getPos();
        GL11.glRotated(MathHelper.wrapDegrees(camera.getPitch()), 1, 0, 0);
        GL11.glRotated(MathHelper.wrapDegrees(camera.getYaw() + 180.0), 0, 1, 0);
        GL11.glTranslated(-camPos.x, -camPos.y, -camPos.z);
    }

    public static void gl11Setup() {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glLineWidth(2.5F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        offsetRender();
    }

    public static void gl11Cleanup() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
}