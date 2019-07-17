package bleach.hack.utils;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

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
		return new Vec3d(ren.getProjectedView().x, ren.getProjectedView().y, ren.getProjectedView().z);
	}
	
	public static void gl11Setup() {
		GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.lineWidth(2.5F);
        GlStateManager.disableTexture();
        GlStateManager.disableDepthTest();
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
	}
	
	public static void gl11Cleanup() {
		GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.enableDepthTest();
        GlStateManager.enableTexture();
        GlStateManager.disableBlend();
	}
}
