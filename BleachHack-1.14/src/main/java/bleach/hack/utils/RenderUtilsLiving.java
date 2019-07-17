package bleach.hack.utils;

import java.util.Map.Entry;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

@SuppressWarnings("deprecation")
public class RenderUtilsLiving {

	private static Minecraft mc = Minecraft.getInstance();
	
	public static void drawText(String str, double x, double y, double z, double scale) {
		glSetup(x, y, z);
		
		GlStateManager.scaled(-0.025*scale, -0.025*scale, 0.025*scale);
	      
		int i = mc.fontRenderer.getStringWidth(str) / 2;
	    GlStateManager.disableTexture();
	    Tessellator tessellator = Tessellator.getInstance();
	    BufferBuilder bufferbuilder = tessellator.getBuffer();
	    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
	    float f = Minecraft.getInstance().gameSettings.func_216840_a(0.25F);
	    bufferbuilder.pos(-i - 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, f).endVertex();
	    bufferbuilder.pos(-i - 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, f).endVertex();
	    bufferbuilder.pos(i + 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, f).endVertex();
	    bufferbuilder.pos(i + 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, f).endVertex();
	    tessellator.draw();
	    GlStateManager.enableTexture();
	      
	    mc.fontRenderer.drawString(str, -i, 0, 553648127);
	    mc.fontRenderer.drawString(str, -i, 0, -1);
	      
	    glCleanup();
	}
	
	public static void drawItem(double x, double y, double z, double offX, double offY, double scale, ItemStack item) {
		glSetup(x, y, z);
		
	    GlStateManager.scaled(0.4*scale, 0.4*scale, 0);
	    
	    GlStateManager.translated(offX, offY, 0);
	    if(item.getItem() instanceof BlockItem) GlStateManager.rotatef(180F, 1F, 180F, 10F);
	    mc.getItemRenderer().renderItem(new ItemStack(item.getItem()), ItemCameraTransforms.TransformType.GUI);
	    if(item.getItem() instanceof BlockItem) GlStateManager.rotatef(-180F, -1F, -180F, -10F);
	    GlStateManager.disableLighting();
	    
	    GlStateManager.scalef(-0.03F, -0.03F, 0);
	    
	    if(item.getCount() > 0) {
		    int w = mc.fontRenderer.getStringWidth("x" + item.getCount()) / 2;
		    mc.fontRenderer.drawStringWithShadow("x" + item.getCount(), 10 - w, 7, 0xffffff);
	    }
	    
	    GlStateManager.scalef(0.8F, 0.8F, 0.8F);
	    
	    int c = 0;
	    for(Entry<Enchantment, Integer> m: EnchantmentHelper.getEnchantments(item).entrySet()) {
	    	int w1 = mc.fontRenderer.getStringWidth(I18n.format(m.getKey().getName()).substring(0, 2) + m.getValue()) / 2;
	    	mc.fontRenderer.drawStringWithShadow(
	    			I18n.format(m.getKey().getName()).substring(0, 2) + m.getValue(), -10 - w1, c*10+11,
	    			m.getKey() == Enchantments.VANISHING_CURSE || m.getKey() == Enchantments.BINDING_CURSE
	    			? 0xff5050 : 0xffb0e0);
	    	c--;
	    }
	    
	    glCleanup();
	}
	
	public static void glSetup(double x, double y, double z) {
		GlStateManager.pushMatrix();
	    GlStateManager.translated(x - RenderUtils.renderPos().x, y - RenderUtils.renderPos().y, z - RenderUtils.renderPos().z);
	    GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
	    GlStateManager.rotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
	    GlStateManager.rotatef(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
	    GlStateManager.disableLighting();
	    GlStateManager.disableDepthTest();
	
	    GlStateManager.enableBlend();
	    GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
	    
	}
	
	public static void glCleanup() {
		GlStateManager.enableLighting();
	    GlStateManager.disableBlend();
	    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	    GlStateManager.enableDepthTest();
	    GlStateManager.translatef(-.5f, 0, 0);
	    GlStateManager.popMatrix();
	}
}
