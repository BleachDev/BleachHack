package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import bleach.hack.utils.RenderUtilsLiving;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
		LivingEntity e = event.getEntity();
		
		/* Color before name */
		String color = e instanceof IMob ? "§5" : EntityUtils.isAnimal(e)
				? "§a" : e.isSneaking() ? "§6" : e instanceof PlayerEntity ? "§c" : "§f";
		
		if(e == mc.player || e == mc.player.getRidingEntity() || color == "§f" || 
				((color == "§c" || color == "§6") && !getSettings().get(4).toToggle().state) ||
				((color == "§5" || color == "§a") && !getSettings().get(5).toToggle().state)) return;
		if(e.isInvisible()) color = "§e";
		
		double scale = (e instanceof PlayerEntity) ?
				Math.max(getSettings().get(2).toSlider().getValue() * (mc.player.getDistance(e) / 20), 1):
				Math.max(getSettings().get(3).toSlider().getValue() * (mc.player.getDistance(e) / 20), 1);
		
		/* Health bar */
		String health = "";
		/* - Add Green Normal Health */
		for(int i = 0; i < e.getHealth(); i++) health += "§a|";
		/* - Add Red Empty Health (Remove Based on absorption amount) */
		for(int i = 0; i < MathHelper.clamp(e.getAbsorptionAmount(), 0, e.getMaxHealth() - e.getHealth()); i++) health += "§e|";
		/* Add Yellow Absorption Health */
		for(int i = 0; i < e.getMaxHealth() - (e.getHealth() + e.getAbsorptionAmount()); i++) health += "§c|";
		/* Add "+??" to the end if the entity has extra hearts */
		if(e.getAbsorptionAmount() - (e.getMaxHealth() - e.getHealth()) > 0) {
			health +=  " §e+" + (int)(e.getAbsorptionAmount() - (e.getMaxHealth() - e.getHealth()));
		}
		
		/* Drawing Nametags */
		if(getSettings().get(1).toMode().mode == 0) {
			RenderUtilsLiving.drawText(color + e.getName().getString() + " [" + (int) (e.getHealth() + e.getAbsorptionAmount()) + "/" + (int) e.getMaxHealth() + "]",
					e.posX,e.posY + e.getHeight() + (0.5f * scale), e.posZ, scale);
		}else if(getSettings().get(1).toMode().mode == 1) {
			RenderUtilsLiving.drawText(color + e.getName().getString(), e.posX, e.posY + e.getHeight() + (0.5f * scale), e.posZ, scale);
			RenderUtilsLiving.drawText(health, e.posX, e.posY + e.getHeight() + (0.75f * scale), e.posZ, scale);
		}
		
		/* Drawing Items */
		double c = 0;
		double higher = getSettings().get(1).toMode().mode == 1 ? 0.25 : 0;
		
		if(getSettings().get(0).toMode().mode == 1) {
			RenderUtilsLiving.drawItem(e.posX, e.posY + e.getHeight() + ((0.75 + higher) * scale), e.posZ, -1.25, 0, scale, e.getHeldItemMainhand());
			RenderUtilsLiving.drawItem(e.posX, e.posY + e.getHeight() + ((0.75 + higher) * scale), e.posZ, 1.25, 0, scale, e.getHeldItemOffhand());
			
			for(ItemStack i: e.getArmorInventoryList()) {
				if(i.getCount() < 1) continue;
				RenderUtilsLiving.drawItem(e.posX, e.posY + e.getHeight() + ((0.75 + higher) * scale), e.posZ, 0, c, scale, i);
				c++;
			}
		}else if(getSettings().get(0).toMode().mode == 2) {
			RenderUtilsLiving.drawItem(e.posX, e.posY + e.getHeight() + ((0.75 + higher) * scale), e.posZ, -2.5, 0, scale, e.getHeldItemMainhand());
			RenderUtilsLiving.drawItem(e.posX, e.posY + e.getHeight() + ((0.75 + higher) * scale), e.posZ, 2.5, 0, scale, e.getHeldItemOffhand());
			
			for(ItemStack i: e.getArmorInventoryList()) {
				RenderUtilsLiving.drawItem(e.posX, e.posY + e.getHeight() + ((0.75 + higher) * scale), e.posZ, c+1.5, 0, scale, i);
				c--;
			}
		}
			
		event.setCanceled(true);
	}
}
