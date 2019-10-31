package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EnderCrystalEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Formatting;

public class ESP extends Module {
	
	public ESP() {
		super("ESP", -1, Category.RENDER, "Allows you to see entities though walls.",
				new SettingToggle("Players", true),
				new SettingToggle("Mobs", false),
				new SettingToggle("Animals", false),
				new SettingToggle("Items", true),
				new SettingToggle("Crystals", true),
				new SettingToggle("Vehicles", false));
	}

	@Override
	public void onDisable() {
		super.onDisable();
		for(Entity e: mc.world.getEntities()) {
			if(e != mc.player) {
				if(e.isGlowing()) e.setGlowing(false);
			}
		}
	}

	@Subscribe
	public void onTick(EventTick event) {
		for(Entity e: mc.world.getEntities()) {
			if(e instanceof PlayerEntity && e != mc.player && getSettings().get(0).toToggle().state) {
				EntityUtils.setGlowing(e, Formatting.RED, "players");
			}
			
			else if(e instanceof Monster && getSettings().get(1).toToggle().state) {
				EntityUtils.setGlowing(e, Formatting.DARK_BLUE, "mobs");
			}
			
			else if(EntityUtils.isAnimal(e) && getSettings().get(2).toToggle().state) {
				EntityUtils.setGlowing(e, Formatting.GREEN, "passive");
			}
			
			else if(e instanceof ItemEntity && getSettings().get(3).toToggle().state) {
				EntityUtils.setGlowing(e, Formatting.GOLD, "items");
			}
			
			else if(e instanceof EnderCrystalEntity && getSettings().get(4).toToggle().state) {
				EntityUtils.setGlowing(e, Formatting.LIGHT_PURPLE, "crystals");
			}
			
			else if((e instanceof BoatEntity || e instanceof AbstractMinecartEntity) && getSettings().get(5).toToggle().state) {
				EntityUtils.setGlowing(e, Formatting.GRAY, "vehicles");
			}
		}
	}
}
