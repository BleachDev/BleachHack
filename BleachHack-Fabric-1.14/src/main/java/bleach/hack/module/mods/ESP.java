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
package bleach.hack.module.mods;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventOutlineColor;
import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingColor;
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

public class ESP extends Module {

	public ESP() {
		super("ESP", KEY_UNBOUND, Category.RENDER, "Allows you to see entities though walls.",
				new SettingToggle("Players", true).withChildren(
						new SettingColor("Player Color", 1f, 0.5f, 0.5f, false),
						new SettingColor("Friend Color", 0f, 1f, 1f, false))
				.withDesc("Show Players"),

				new SettingToggle("Mobs", false).withChildren(
						new SettingColor("Color", 0.5f, 0.1f, 0.5f, false))
				.withDesc("Show Mobs"),

				new SettingToggle("Animals", false).withChildren(
						new SettingColor("Color", 0.3f, 1f, 0.3f, false))
				.withDesc("Show Animals"),

				new SettingToggle("Items", true).withChildren(
						new SettingColor("Color", 1f, 0.8f, 0.2f, false))
				.withDesc("Show Items"),

				new SettingToggle("Crystals", true).withChildren(
						new SettingColor("Color", 1f, 0.2f, 1f, false))
				.withDesc("Show End Crystals"),

				new SettingToggle("Vehicles", false).withChildren(
						new SettingColor("Color", 0.6f, 0.6f, 0.6f, false))
				.withDesc("Show Vehicles"));
	}

	@Override
	public void onDisable() {
		super.onDisable();
		for (Entity e: mc.world.getEntities()) {
			if (e != mc.player) {
				if (e.isGlowing()) e.setGlowing(false);
			}
		}
	}
	
	@Subscribe
	public void onTick(EventTick event) {
		for (Entity e: mc.world.getEntities()) {
			if ((e instanceof PlayerEntity && getSettings().get(0).asToggle().state)
					|| (e instanceof Monster && getSettings().get(1).asToggle().state)
					|| (EntityUtils.isAnimal(e) && getSettings().get(2).asToggle().state)
					|| (e instanceof ItemEntity && getSettings().get(3).asToggle().state)
					|| (e instanceof EnderCrystalEntity && getSettings().get(4).asToggle().state)
					|| ((e instanceof BoatEntity || e instanceof AbstractMinecartEntity) && getSettings().get(5).asToggle().state)) {
				e.setGlowing(true);
			}
		}
	}

	@Subscribe
	public void onOutlineColor(EventOutlineColor event) {
		for (Entity e: mc.world.getEntities()) {
			boolean glow = true;
			
			if (e instanceof PlayerEntity && e != mc.player && getSettings().get(0).asToggle().state) {
				if (BleachHack.friendMang.has(e.getName().asString())) {
					event.color = getSettings().get(0).getChild(1).asColor().getRGB();
				} else {
					event.color = getSettings().get(0).getChild(0).asColor().getRGB();
				}
			} else if (e instanceof Monster && getSettings().get(1).asToggle().state) {
				event.color = getSettings().get(1).getChild(0).asColor().getRGB();
			} else if (EntityUtils.isAnimal(e) && getSettings().get(2).asToggle().state) {
				event.color = getSettings().get(2).getChild(0).asColor().getRGB();
			} else if (e instanceof ItemEntity && getSettings().get(3).asToggle().state) {
				event.color = getSettings().get(3).getChild(0).asColor().getRGB();
			}else if (e instanceof EnderCrystalEntity && getSettings().get(4).asToggle().state) {
				event.color = getSettings().get(4).getChild(0).asColor().getRGB();
			} else if ((e instanceof BoatEntity || e instanceof AbstractMinecartEntity) && getSettings().get(5).asToggle().state) {
				event.color = getSettings().get(5).getChild(0).asColor().getRGB();
			} else {
				glow = false;
			}
			
			if (glow) e.setGlowing(true);
		}
	}
}
