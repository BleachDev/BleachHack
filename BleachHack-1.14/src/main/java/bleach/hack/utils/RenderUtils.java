package bleach.hack.utils;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class RenderUtils {

	private static Minecraft mc = Minecraft.getInstance();
	
	public static void drawFilledBox(BlockPos blockPos, float r, float g, float b, float a) {
		drawFilledBox(new AxisAlignedBB(
				blockPos.getX(), blockPos.getY(), blockPos.getZ(),
				blockPos.getX()+1, blockPos.getY()+1, blockPos.getZ()+1), r, g, b, a);
	}
	
	public static void drawFilledBox(AxisAlignedBB box, float r, float g, float b, float a) {
		double x = box.minX - getRenderPos()[0];
		double y = box.minY - getRenderPos()[1];
	    double z = box.minZ - getRenderPos()[2];
        
        gl11Setup();
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);
        WorldRenderer.addChainedFilledBoxVertices(bufferbuilder,
        		x, y, z, x+box.getXSize(), y+box.getYSize(), z+box.getZSize(), r, g, b, a/2f);
        tessellator.draw();

        WorldRenderer.drawSelectionBoundingBox(new AxisAlignedBB(
        		x, y, z, x+box.getXSize(), y+box.getYSize(), z+box.getZSize()), r, g, b, a);

        gl11Cleanup();
    }
	
	public static void drawSelectionBox(BlockPos blockPos, float r, float g, float b, float a) {
		drawSelectionBox(new AxisAlignedBB(
				blockPos.getX(), blockPos.getY(), blockPos.getZ(),
				blockPos.getX()+1, blockPos.getY()+1, blockPos.getZ()+1), r, g, b, a);
	}
	
	public static void drawSelectionBox(AxisAlignedBB box, float r, float g, float b, float a) {
		double x = box.minX - getRenderPos()[0];
		double y = box.minY - getRenderPos()[1];
		double z = box.minZ - getRenderPos()[2];
        
        gl11Setup();
        WorldRenderer.drawSelectionBoundingBox(new AxisAlignedBB(
        		x, y, z, x+box.getXSize(), y+box.getYSize(), z+box.getZSize()), r, g, b, a);

        gl11Cleanup();
    }
	
	public static double[] getRenderPos() {
		double x = 0, y = 0, z = 0;
		try { x = (double) FieldUtils.readField(mc.getRenderManager(), "renderPosX", true);
			y = (double) FieldUtils.readField(mc.getRenderManager(), "renderPosY", true);
	        z = (double) FieldUtils.readField(mc.getRenderManager(), "renderPosZ", true);
		} catch (Exception e) {}
		return new double[] {x,y,z};
	}
	
	public static void gl11Setup() {
		GL11.glBlendFunc(770, 771);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0F);
	}
	
	public static void gl11Cleanup() {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
	}
}
