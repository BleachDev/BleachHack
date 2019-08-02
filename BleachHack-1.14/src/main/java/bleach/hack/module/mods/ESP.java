package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;

public class ESP extends Module {
	
	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(true, "Players"),
			new SettingToggle(false, "Mobs"),
			new SettingToggle(false, "Animals"),
			new SettingToggle(true, "Items"),
			new SettingToggle(true, "Crystals"),
			new SettingToggle(false, "Vehicles"));
	
	public ESP() {
		super("ESP", -1, Category.RENDER, "Allows you to see entities though walls.", settings);
	}
	
	public void onUpdate() {
		if(this.isToggled()) {
			for(Entity e: EntityUtils.getLoadedEntities()) {
				if(e instanceof PlayerEntity && e != mc.player && getSettings().get(0).toToggle().state) {
					EntityUtils.setGlowing(e, TextFormatting.RED, "players");
				}
				
				if(e instanceof IMob && getSettings().get(1).toToggle().state) {
					EntityUtils.setGlowing(e, TextFormatting.DARK_BLUE, "mobs");
				}
				
				if(EntityUtils.isAnimal(e) && getSettings().get(2).toToggle().state) {
					EntityUtils.setGlowing(e, TextFormatting.GREEN, "passive");
				}
				
				if(e instanceof ItemEntity && getSettings().get(3).toToggle().state) {
					EntityUtils.setGlowing(e, TextFormatting.GOLD, "items");
				}
				
				if(e instanceof EnderCrystalEntity && getSettings().get(4).toToggle().state) {
					EntityUtils.setGlowing(e, TextFormatting.LIGHT_PURPLE, "crystals");
				}
				if((e instanceof BoatEntity || e instanceof AbstractMinecartEntity) && getSettings().get(5).toToggle().state) {
					EntityUtils.setGlowing(e, TextFormatting.GRAY, "vehicles");
				}
			}
		}
	}
	
	public void onDisable() {
		for(Entity e: EntityUtils.getLoadedEntities()) {
			if(e != mc.player) {
				if(e.isGlowing()) e.setGlowing(false);
			}
		}
	}

}
