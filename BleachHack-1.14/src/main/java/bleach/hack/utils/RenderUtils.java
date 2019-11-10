package bleach.hack.utils;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class RenderUtils {

	private static Minecraft mc = Minecraft.getInstance();
	
	public static void drawFilledBox(BlockPos blockPos, float r, float g, float b, float a) {
		drawFilledBox(new AxisAlignedBB(
				blockPos.getX(), blockPos.getY(), blockPos.getZ(),
				blockPos.getX()+1, blockPos.getY()+1, blockPos.getZ()+1), r, g, b, a);
	}
	
	public static void drawFilledBox(AxisAlignedBB box, float r, float g, float b, float a) {
		gl11Setup();
		
		Vec3d ren = renderPos();

        /* Fill */
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(5, DefaultVertexFormats.POSITION_COLOR);
        WorldRenderer.addChainedFilledBoxVertices(buffer,
        		box.minX - ren.x, box.minY - ren.y, box.minZ - ren.z,
        		box.maxX - ren.x, box.maxY - ren.y, box.maxZ - ren.z, r, g, b, a/2f);
        tessellator.draw();
        
        /* Outline */
        WorldRenderer.drawSelectionBoundingBox(new AxisAlignedBB(
        		box.minX - ren.x, box.minY - ren.y, box.minZ - ren.z,
        		box.maxX - ren.x, box.maxY - ren.y, box.maxZ - ren.z), r, g, b, a);

        gl11Cleanup();
    }
	
	// Don't need the code if we don't use it.
	/*public static void drawSelectionBox(BlockPos blockPos, float r, float g, float b, float a) {
		drawSelectionBox(new AxisAlignedBB(
				blockPos.getX(), blockPos.getY(), blockPos.getZ(),
				blockPos.getX()+1, blockPos.getY()+1, blockPos.getZ()+1), r, g, b, a);
	}
	
	public static void drawSelectionBox(AxisAlignedBB box, float r, float g, float b, float a) {
		gl11Setup();
		
		Vec3d ren = renderPos();
        
        WorldRenderer.drawSelectionBoundingBox(new AxisAlignedBB(
        		box.minX - ren.x, box.minY - ren.y, box.minZ - ren.z,
        		box.maxX - ren.x, box.maxY - ren.y, box.maxZ - ren.z), r, g, b, a);

        gl11Cleanup();
    }*/
	
	public static void drawLine(double x1,double y1,double z1,double x2,double y2,double z2, float r, float g, float b, float t) {
		gl11Setup();
		GL11.glLineWidth(t);
        
		Vec3d ren = renderPos();
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x1 - ren.x, y1 - ren.y, z1 - ren.z).color(r, g, b, 0.0F).endVertex();
        buffer.pos(x1 - ren.x, y1 - ren.y, z1 - ren.z).color(r, g, b, 1.0F).endVertex();
        buffer.pos(x2 - ren.x, y2 - ren.y, z2 - ren.z).color(r, g, b, 1.0F).endVertex();
        tessellator.draw();
        
		gl11Cleanup();
        
	}
	
	public static Vec3d renderPos() {
		ActiveRenderInfo ren = mc.gameRenderer.getActiveRenderInfo();
		return ren.getProjectedView();
	}
	
	public static void gl11Setup() {
		GL11.glEnable(GL11.GL_BLEND);
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glLineWidth(2.5F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glMatrixMode(5889);
        GL11.glPushMatrix();
	}
	
	public static void gl11Cleanup() {
		GL11.glPopMatrix();
		GL11.glMatrixMode(5888);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
