package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import com.mojang.blaze3d.platform.GlStateManager;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@SuppressWarnings("deprecation")
public class Nametags extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"Simple", "Armor V", "Armor H"}, "Mode: "),
			new SettingMode(new String[] {"Number", "Bar"}, "Health: "),
			new SettingSlider(0.5, 5, 2, 1, "Size Players: "),
			new SettingSlider(0.5, 5, 1, 1, "Size Mobs: "),
			new SettingToggle(true, "Players"),
			new SettingToggle(false, "Mobs"));
	
	public Nametags() {
		super("Nametags", -1, Category.RENDER, "Shows bigger/cooler nametags above entities.", settings);
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
		String color = e instanceof IMob ? "§5" : EntityUtils.isAnimal(e)
				? "§a" : e.isSneaking() ? "§6" : e instanceof PlayerEntity ? "§c" : "§f";
		
		if(e == mc.player || color == "§f" || 
				((color == "§c" || color == "§6") && !getSettings().get(4).toToggle().state) ||
				((color == "§5" || color == "§a") && !getSettings().get(5).toToggle().state)) return;
		
		
		double scale = (e instanceof PlayerEntity) ? getSettings().get(2).toSlider().getValue() :
	    	getSettings().get(3).toSlider().getValue();
		
		/* Health bar */
		String health = "";
		for(int i = 0; i < e.getHealth(); i++) health += "§a|";
		for(int i = 0; i < MathHelper.clamp(e.getAbsorptionAmount(), 0, e.getMaxHealth() - e.getHealth()); i++) health += "§e|";
		for(int i = 0; i < e.getMaxHealth() - (e.getHealth() + e.getAbsorptionAmount()); i++) health += "§c|";
		if(e.getAbsorptionAmount() - (e.getMaxHealth() - e.getHealth()) > 0) {
			health +=  " §e+" + (int)(e.getAbsorptionAmount() - (e.getMaxHealth() - e.getHealth()));
		}
		
		/* Drawing Nametags */
		if(getSettings().get(1).toMode().mode == 0) {
			drawNameplate(color + e.getName().getString() + " [" + (int) (e.getHealth() + e.getAbsorptionAmount()) + "/" + (int) e.getMaxHealth() + "]",
					e.posX - x,e.posY - y + e.getHeight() + (0.5f * scale), e.posZ - z, scale);
		}else if(getSettings().get(1).toMode().mode == 1) {
			drawNameplate(color + e.getName().getString(), e.posX - x, e.posY - y + e.getHeight() + (0.5f * scale), e.posZ - z, scale);
			drawNameplate(health, e.posX - x, e.posY - y + e.getHeight() + (0.75f * scale), e.posZ - z, scale);
		}
		
		/* Drawing Items */
		double c = 0;
		double higher = getSettings().get(1).toMode().mode == 1 ? 0.25 : 0;
		
		if(getSettings().get(0).toMode().mode == 1) {
			drawItem(e.posX - x, e.posY - y + e.getHeight() + ((0.75 + higher) * scale), e.posZ - z, -1.25, 0, scale, e.getHeldItemMainhand());
			drawItem(e.posX - x, e.posY - y + e.getHeight() + ((0.75 + higher) * scale), e.posZ - z, 1.25, 0, scale, e.getHeldItemOffhand());
			
			for(ItemStack i: e.getArmorInventoryList()) {
				if(i.getCount() < 1) continue;
				drawItem(e.posX - x, e.posY - y + e.getHeight() + ((0.75 + higher) * scale), e.posZ - z, 0, c, scale, i);
				c++;
			}
		}else if(getSettings().get(0).toMode().mode == 2) {
			drawItem(e.posX - x, e.posY - y + e.getHeight() + ((0.75 + higher) * scale), e.posZ - z, -2.5, 0, scale, e.getHeldItemMainhand());
			drawItem(e.posX - x, e.posY - y + e.getHeight() + ((0.75 + higher) * scale), e.posZ - z, 2.5, 0, scale, e.getHeldItemOffhand());
			
			for(ItemStack i: e.getArmorInventoryList()) {
				drawItem(e.posX - x, e.posY - y + e.getHeight() + ((0.75 + higher) * scale), e.posZ - z, c+1.5, 0, scale, i);
				c--;
			}
		}
			
		event.setCanceled(true);
	}
	
	public void drawNameplate(String str, double x, double y, double z, double scale) {
	      GlStateManager.pushMatrix();
	      GlStateManager.translated(x, y, z);
	      GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
	      GlStateManager.rotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
	      GlStateManager.rotatef(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
	      GlStateManager.scaled(-0.025*scale, -0.025*scale, 0.025*scale);
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
	      bufferbuilder.pos(-i - 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, f).endVertex();
	      bufferbuilder.pos(-i - 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, f).endVertex();
	      bufferbuilder.pos(i + 1, 8, 0.0D).color(0.0F, 0.0F, 0.0F, f).endVertex();
	      bufferbuilder.pos(i + 1, -1, 0.0D).color(0.0F, 0.0F, 0.0F, f).endVertex();
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
	
	public void drawItem(double x, double y, double z, double offX, double offY, double scale, ItemStack item) {
		GlStateManager.pushMatrix();
	    GlStateManager.translated(x, y, z);
	    GlStateManager.normal3f(0.0F, 1.0F, 0.0F);
	    GlStateManager.rotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
	    GlStateManager.rotatef(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
	    GlStateManager.scaled(0.4*scale, 0.4*scale, 0);
	    GlStateManager.disableLighting();
	    GlStateManager.disableDepthTest();
	
	    GlStateManager.enableBlend();
	    GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
	    
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
	    
	    GlStateManager.enableLighting();
	    GlStateManager.disableBlend();
	    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	    GlStateManager.enableDepthTest();
	    GlStateManager.translatef(-.5f, 0, 0);
	    GlStateManager.popMatrix();
	}

}
