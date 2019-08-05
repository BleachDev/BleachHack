package bleach.hack.utils;

import java.util.Map.Entry;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.json.ModelTransformation.Type;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

public class RenderUtilsLiving {

	private static MinecraftClient mc = MinecraftClient.getInstance();
	
	public static void drawText(String str, double x, double y, double z, double scale) {
		glSetup(x, y, z);
		
		GlStateManager.scaled(-0.025*scale, -0.025*scale, 0.025*scale);
	      
		int i = mc.textRenderer.getStringWidth(str) / 2;
	    GlStateManager.disableTexture();
	    Tessellator tessellator = Tessellator.getInstance();
	    BufferBuilder bufferbuilder = tessellator.getBufferBuilder();
	    bufferbuilder.begin(7, VertexFormats.POSITION_COLOR);
	    float f = mc.options.getTextBackgroundOpacity(0.25F);
	    bufferbuilder.vertex(-i - 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, f).next();
	    bufferbuilder.vertex(-i - 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, f).next();
	    bufferbuilder.vertex(i + 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, f).next();
	    bufferbuilder.vertex(i + 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, f).next();
	    tessellator.draw();
	    GlStateManager.enableTexture();
	      
	    mc.textRenderer.draw(str, -i, 0, 553648127);
	    mc.textRenderer.draw(str, -i, 0, -1);
	      
	    glCleanup();
	}
	
	public static void drawItem(double x, double y, double z, double offX, double offY, double scale, ItemStack item) {
		glSetup(x, y, z);
		
	    GlStateManager.scaled(0.4*scale, 0.4*scale, 0);
	    
	    GlStateManager.translated(offX, offY, 0);
	    if(item.getItem() instanceof BlockItem) GlStateManager.rotatef(180F, 1F, 180F, 10F);
	    mc.getItemRenderer().renderItem(new ItemStack(item.getItem()), Type.GUI);
	    if(item.getItem() instanceof BlockItem) GlStateManager.rotatef(-180F, -1F, -180F, -10F);
	    GlStateManager.disableLighting();
	    
	    GlStateManager.scalef(-0.05F, -0.05F, 0);
	    
	    if(item.getCount() > 0) {
		    int w = mc.textRenderer.getStringWidth("x" + item.getCount()) / 2;
		    mc.textRenderer.drawWithShadow("x" + item.getCount(), 7 - w, 5, 0xffffff);
	    }
	    
	    GlStateManager.scalef(0.85F, 0.85F, 0.85F);
	    
	    int c = 0;
	    for(Entry<Enchantment, Integer> m: EnchantmentHelper.getEnchantments(item).entrySet()) {
	    	int w1 = mc.textRenderer.getStringWidth(I18n.translate(m.getKey().getName(2).asString()).substring(0, 2) + m.getValue()) / 2;
	    	mc.textRenderer.drawWithShadow(
	    			I18n.translate(m.getKey().getName(2).asString()).substring(0, 2) + m.getValue(), -4 - w1, c*10-1,
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
	    GlStateManager.rotatef(-mc.player.yaw, 0.0F, 1.0F, 0.0F);
	    GlStateManager.rotatef(mc.player.pitch, 1.0F, 0.0F, 0.0F);
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
