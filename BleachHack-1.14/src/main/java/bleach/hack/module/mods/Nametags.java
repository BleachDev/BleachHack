package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Nametags extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"Number", "Bar"}, "Health: "));
	
	public Nametags() {
		super("Nametags", -1, Category.RENDER, "Shows bigger/cooler nametags above players.", settings);
	}
	
	public void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@SubscribeEvent
	public void renderTag(RenderLivingEvent.Specials.Pre<?, ?> event) {
		double x = mc.gameRenderer.getActiveRenderInfo().getProjectedView().x;
	    double y = mc.gameRenderer.getActiveRenderInfo().getProjectedView().y;
	    double z = mc.gameRenderer.getActiveRenderInfo().getProjectedView().z;
			
		LivingEntity e = event.getEntity();
		
		/* Color before name */
		String color = "§f";
		if(e instanceof IMob) color = "§5";
		else if(EntityUtils.isAnimal(e)) color = "§a";
		if(e instanceof PlayerEntity) color = "§c";
		
		/* Health bar */
		String health = "";
		for(int i = 0; i < e.getHealth(); i++) health += "§a|";
		for(int i = 0; i < MathHelper.clamp(e.getAbsorptionAmount(), 0, e.getMaxHealth() - e.getHealth()); i++) health += "§e|";
		for(int i = 0; i < e.getMaxHealth() - (e.getHealth() + e.getAbsorptionAmount()); i++) health += "§c|";
		if(e.getAbsorptionAmount() - (e.getMaxHealth() - e.getHealth()) > 0) {
			health +=  " §e+" + (int)(e.getAbsorptionAmount() - (e.getMaxHealth() - e.getHealth()));
		}
		
		/* Drawing Nametags */
		if(getSettings().get(0).toMode().mode == 0) {
			drawNameplate(
					color + e.getName().getString() + " [" + (int) (e.getHealth() + e.getAbsorptionAmount()) + "/" + (int) e.getMaxHealth() + "]",
					(float)(e.posX - x), (float)(e.posY - y)+e.getHeight()+0.5f, (float)(e.posZ - z));
		}else if(getSettings().get(0).toMode().mode == 1) {
			drawNameplate(
					color + e.getName().getString(),
					(float)(e.posX - x), (float)(e.posY - y)+e.getHeight()+0.5f, (float)(e.posZ - z));
			
			drawNameplate(health, (float)(e.posX - x), (float)(e.posY - y)+e.getHeight()+0.75f, (float)(e.posZ - z));
		}
			
		event.setCanceled(true);
	}
	
	public void drawNameplate(String str, float x, float y, float z) {
	      GlStateManager.pushMatrix();
	      GlStateManager.translatef(x, y, z);
	      GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
	      GlStateManager.rotatef(-mc.player.rotationYaw, 0.0F, 1.0F, 0.0F);
	      GlStateManager.rotatef(mc.player.rotationPitch, 1.0F, 0.0F, 0.0F);
	      GlStateManager.scalef(-0.025F, -0.025F, 0.025F);
	      GlStateManager.disableLighting();
	      GlStateManager.disableDepthTest();

	      GlStateManager.enableBlend();
	      GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
	      int i = mc.fontRenderer.getStringWidth(str) / 2;
	      GlStateManager.disableTexture();
	      Tessellator tessellator = Tessellator.getInstance();
	      BufferBuilder bufferbuilder = tessellator.getBuffer();
	      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
	      float f = Minecraft.getInstance().gameSettings.func_216840_a(0.25F);
	      bufferbuilder.pos((double)(-i - 1), -1, 0.0D).color(0.0F, 0.0F, 0.0F, f).endVertex();
	      bufferbuilder.pos((double)(-i - 1), 8, 0.0D).color(0.0F, 0.0F, 0.0F, f).endVertex();
	      bufferbuilder.pos((double)(i + 1), 8, 0.0D).color(0.0F, 0.0F, 0.0F, f).endVertex();
	      bufferbuilder.pos((double)(i + 1), -1, 0.0D).color(0.0F, 0.0F, 0.0F, f).endVertex();
	      tessellator.draw();
	      GlStateManager.enableTexture();
	      
	      mc.fontRenderer.drawString(str, -i, 0, 553648127);

	      mc.fontRenderer.drawString(str, -i, 0, -1);
	      GlStateManager.enableLighting();
	      GlStateManager.disableBlend();
	      GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	      GlStateManager.enableDepthTest();
	      GlStateManager.popMatrix();
	   }

}
