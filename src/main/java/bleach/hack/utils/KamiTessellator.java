package bleach.hack.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.BlockPos;

import static org.lwjgl.opengl.GL11.*;

public class KamiTessellator extends Tessellator {

    public static KamiTessellator INSTANCE = new KamiTessellator();

    public KamiTessellator() {
        super(0x200000);
    }

    public static void prepare(int mode) {
        prepareGL();
        begin(mode);
    }

    public static void prepareGL() {
//        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        //GLStateManager.SourceFactor and GLStateManager.DestFactor don't exist anymore
        GlStateManager.blendFuncSeparate(770, 771, 1, 0);
        GlStateManager.lineWidth(1.5F);
        GlStateManager.disableTexture();
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.disableDepthTest();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableAlphaTest();
        GlStateManager.color4f(1f,1f,1f,1.0f);
    }

    public static void begin(int mode) {
        INSTANCE.getBuffer().begin(mode, VertexFormats.POSITION_COLOR);
    }

    //TODO FIX!!
    //public static void release() {
    //    render();
    //    releaseGL();
    //}

    //TODO FIX!!
    //public static void render() {
    //    INSTANCE.draw();
    //}

    //TODO FIX!!
    //public static void releaseGL() {
    //    GlStateManager.enableCull();
    //    GlStateManager.depthMask(true);
    //    GlStateManager.enableTexture();
    //    GlStateManager.enableBlend();
    //    GlStateManager.enableDepthTest();
    //}

    //public static void drawBox(BlockPos blockPos, int argb, int sides) {
    //    final int a = (argb >>> 24) & 0xFF;
    //    final int r = (argb >>> 16) & 0xFF;
    //    final int g = (argb >>> 8) & 0xFF;
    //    final int b = argb & 0xFF;
    //    drawBox(blockPos, r, g, b, a, sides);
    //}

    //public void drawBox(float x, float y, float z, int argb, int sides) {
    //    final int a = (argb >>> 24) & 0xFF;
    //    final int r = (argb >>> 16) & 0xFF;
    //    final int g = (argb >>> 8) & 0xFF;
    //    final int b = argb & 0xFF;
    //    drawBox(getBuffer(), x, y, z, 1, 1, 1, r, g, b, a, sides);
    //}

    //TODO FIX!!
    //public static void drawBox(BlockPos blockPos, int r, int g, int b, int a, int sides) {
    //    drawBox(INSTANCE.getBuffer(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1, 1, 1, r, g, b, a, sides);
    //}

    //TODO FIX!!
    //I removed static references because static getBuffer can't overwrite getBuffer(?)
    //public BufferBuilder getBuffer() {
    //    return INSTANCE.getBuffer();
    //}

    public static void drawBox(final BufferBuilder buffer, float x, float y, float z, float w, float h, float d, int r, int g, int b, int a, int sides) {
        if ((sides & GeometryMasks.Quad.DOWN) != 0) {
            buffer.vertex(x + w, y, z).color(r, g, b, a).next();
            buffer.vertex(x + w, y, z + d).color(r, g, b, a).next();
            buffer.vertex(x, y, z + d).color(r, g, b, a).next();
            buffer.vertex(x, y, z).color(r, g, b, a).next();
        }

        if ((sides & GeometryMasks.Quad.UP) != 0) {
            buffer.vertex(x + w, y + h, z).color(r, g, b, a).next();
            buffer.vertex(x, y + h, z).color(r, g, b, a).next();
            buffer.vertex(x, y + h, z + d).color(r, g, b, a).next();
            buffer.vertex(x + w, y + h, z + d).color(r, g, b, a).next();
        }

        if ((sides & GeometryMasks.Quad.NORTH) != 0) {
            buffer.vertex(x + w, y, z).color(r, g, b, a).next();
            buffer.vertex(x, y, z).color(r, g, b, a).next();
            buffer.vertex(x, y + h, z).color(r, g, b, a).next();
            buffer.vertex(x + w, y + h, z).color(r, g, b, a).next();
        }

        if ((sides & GeometryMasks.Quad.SOUTH) != 0) {
            buffer.vertex(x, y, z + d).color(r, g, b, a).next();
            buffer.vertex(x + w, y, z + d).color(r, g, b, a).next();
            buffer.vertex(x + w, y + h, z + d).color(r, g, b, a).next();
            buffer.vertex(x, y + h, z + d).color(r, g, b, a).next();
        }

        if ((sides & GeometryMasks.Quad.WEST) != 0) {
            buffer.vertex(x, y, z).color(r, g, b, a).next();
            buffer.vertex(x, y, z + d).color(r, g, b, a).next();
            buffer.vertex(x, y + h, z + d).color(r, g, b, a).next();
            buffer.vertex(x, y + h, z).color(r, g, b, a).next();
        }

        if ((sides & GeometryMasks.Quad.EAST) != 0) {
            buffer.vertex(x + w, y, z + d).color(r, g, b, a).next();
            buffer.vertex(x + w, y, z).color(r, g, b, a).next();
            buffer.vertex(x + w, y + h, z).color(r, g, b, a).next();
            buffer.vertex(x + w, y + h, z + d).color(r, g, b, a).next();
        }
    }

    public static void drawLines(final BufferBuilder buffer, float x, float y, float z, float w, float h, float d, int r, int g, int b, int a, int sides) {
        if ((sides & GeometryMasks.Line.DOWN_WEST) != 0) {
            buffer.vertex(x, y, z).color(r, g, b, a).next();
            buffer.vertex(x, y, z + d).color(r, g, b, a).next();
        }

        if ((sides & GeometryMasks.Line.UP_WEST) != 0) {
            buffer.vertex(x, y + h, z).color(r, g, b, a).next();
            buffer.vertex(x, y + h, z + d).color(r, g, b, a).next();
        }

        if ((sides & GeometryMasks.Line.DOWN_EAST) != 0) {
            buffer.vertex(x + w, y, z).color(r, g, b, a).next();
            buffer.vertex(x + w, y, z + d).color(r, g, b, a).next();
        }

        if ((sides & GeometryMasks.Line.UP_EAST) != 0) {
            buffer.vertex(x + w, y + h, z).color(r, g, b, a).next();
            buffer.vertex(x + w, y + h, z + d).color(r, g, b, a).next();
        }

        if ((sides & GeometryMasks.Line.DOWN_NORTH) != 0) {
            buffer.vertex(x, y, z).color(r, g, b, a).next();
            buffer.vertex(x + w, y, z).color(r, g, b, a).next();
        }

        if ((sides & GeometryMasks.Line.UP_NORTH) != 0) {
            buffer.vertex(x, y + h, z).color(r, g, b, a).next();
            buffer.vertex(x + w, y + h, z).color(r, g, b, a).next();
        }

        if ((sides & GeometryMasks.Line.DOWN_SOUTH) != 0) {
            buffer.vertex(x, y, z + d).color(r, g, b, a).next();
            buffer.vertex(x + w, y, z + d).color(r, g, b, a).next();
        }

        if ((sides & GeometryMasks.Line.UP_SOUTH) != 0) {
            buffer.vertex(x, y + h, z + d).color(r, g, b, a).next();
            buffer.vertex(x + w, y + h, z + d).color(r, g, b, a).next();
        }

        if ((sides & GeometryMasks.Line.NORTH_WEST) != 0) {
            buffer.vertex(x, y, z).color(r, g, b, a).next();
            buffer.vertex(x, y + h, z).color(r, g, b, a).next();
        }

        if ((sides & GeometryMasks.Line.NORTH_EAST) != 0) {
            buffer.vertex(x + w, y, z).color(r, g, b, a).next();
            buffer.vertex(x + w, y + h, z).color(r, g, b, a).next();
        }

        if ((sides & GeometryMasks.Line.SOUTH_WEST) != 0) {
            buffer.vertex(x, y, z + d).color(r, g, b, a).next();
            buffer.vertex(x, y + h, z + d).color(r, g, b, a).next();
        }

        if ((sides & GeometryMasks.Line.SOUTH_EAST) != 0) {
            buffer.vertex(x + w, y, z + d).color(r, g, b, a).next();
            buffer.vertex(x + w, y + h, z + d).color(r, g, b, a).next();
        }
    }

}
