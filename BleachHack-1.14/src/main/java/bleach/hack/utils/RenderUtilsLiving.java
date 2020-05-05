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

import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

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
		
		GL11.glScaled(-0.025*scale, -0.025*scale, 0.025*scale);
	      
		int i = mc.fontRenderer.getStringWidth(str) / 2;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	    Tessellator tessellator = Tessellator.getInstance();
	    BufferBuilder bufferbuilder = tessellator.getBuffer();
	    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
	    float f = Minecraft.getInstance().gameSettings.func_216840_a(0.25F);
	    bufferbuilder.pos(-i - 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, f).endVertex();
	    bufferbuilder.pos(-i - 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, f).endVertex();
	    bufferbuilder.pos(i + 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, f).endVertex();
	    bufferbuilder.pos(i + 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, f).endVertex();
	    tessellator.draw();
	    GL11.glEnable(GL11.GL_TEXTURE_2D);
	      
	    mc.fontRenderer.drawString(str, -i, 0, 553648127);
	    mc.fontRenderer.drawString(str, -i, 0, -1);
	      
	    glCleanup();
	}
	
	public static void drawItem(double x, double y, double z, double offX, double offY, double scale, ItemStack item) {
		glSetup(x, y, z);
		
	    GL11.glScaled(0.4*scale, 0.4*scale, 0);
	    
	    GL11.glTranslated(offX, offY, 0);
	    if (item.getItem() instanceof BlockItem) GL11.glRotatef(180F, 1F, 180F, 10F);
	    mc.getItemRenderer().renderItem(new ItemStack(item.getItem()), ItemCameraTransforms.TransformType.GUI);
	    if (item.getItem() instanceof BlockItem) GL11.glRotatef(-180F, -1F, -180F, -10F);
	    GL11.glDisable(GL11.GL_LIGHTING);
	    
	    GL11.glScalef(-0.05F, -0.05F, 0);
	    
	    if (item.getCount() > 0) {
		    int w = mc.fontRenderer.getStringWidth("x" + item.getCount()) / 2;
		    mc.fontRenderer.drawStringWithShadow("x" + item.getCount(), 7 - w, 5, 0xffffff);
	    }
	    
	    GL11.glScalef(0.85F, 0.85F, 0.85F);
	    
	    int c = 0;
	    for (Entry<Enchantment, Integer> m: EnchantmentHelper.getEnchantments(item).entrySet()) {
	    	int w1 = mc.fontRenderer.getStringWidth(I18n.format(m.getKey().getName()).substring(0, 2) + m.getValue()) / 2;
	    	mc.fontRenderer.drawStringWithShadow(
	    			I18n.format(m.getKey().getName()).substring(0, 2) + m.getValue(), -4 - w1, c*10-1,
	    			m.getKey() == Enchantments.VANISHING_CURSE || m.getKey() == Enchantments.BINDING_CURSE
	    			? 0xff5050 : 0xffb0e0);
	    	c--;
	    }
	    
	    glCleanup();
	}
	
	public static void glSetup(double x, double y, double z) {
		GL11.glPushMatrix();
	    GL11.glTranslated(x - RenderUtils.renderPos().x, y - RenderUtils.renderPos().y, z - RenderUtils.renderPos().z);
	    GL11.glNormal3f(0.0F, 1.0F, 0.0F);
	    GL11.glRotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
	    GL11.glRotatef(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
	    GL11.glDisable(GL11.GL_LIGHTING);
	    GL11.glDisable(GL11.GL_DEPTH_TEST);
	
	    GL11.glEnable(GL11.GL_BLEND);
	    GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
	    
	}
	
	public static void glCleanup() {
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
	    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	    GL11.glEnable(GL11.GL_DEPTH_TEST);
	    GL11.glTranslatef(-.5f, 0, 0);
	    GL11.glPopMatrix();
	}
}
