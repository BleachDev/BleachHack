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
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.gui.clickgui.SettingColor;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import bleach.hack.utils.RenderUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

public class Tracers extends Module {

	public Tracers() {
		super("Tracers", KEY_UNBOUND, Category.RENDER, "Shows lines to entities you select.",
				new SettingToggle("Players", true).withChildren(
						new SettingColor("Player Color", 1f, 0.3f, 0.3f, false).withDesc("Tracer color for players"),
						new SettingColor("Friend Color", 0f, 1f, 1f, false).withDesc("Tracer color for friends"))
				.withDesc("Show Player Tracers"),
				
				new SettingToggle("Mobs", false).withChildren(
						new SettingColor("Color", 0.5f, 0.1f, 0.5f, false).withDesc("Tracer color for mobs"))
				.withDesc("Show Mob Tracers"),
				
				new SettingToggle("Animals", false).withChildren(
						new SettingColor("Color", 0.3f, 1f, 0.3f, false).withDesc("Tracer color for animals"))
				.withDesc("Show Animal Tracers"),
				
				new SettingToggle("Items", true).withChildren(
						new SettingColor("Color", 1f, 0.8f, 0.2f, false).withDesc("Tracer color for items"))
				.withDesc("Show Item Tracers"),
				
				new SettingToggle("Crystals", true).withChildren(
						new SettingColor("Color", 1f, 0.2f, 1f, false).withDesc("Tracer color for crystals"))
				.withDesc("Show End Crystal Tracers"),
				
				new SettingToggle("Vehicles", false).withChildren(
						new SettingColor("Color", 0.6f, 0.6f, 0.6f, false).withDesc("Tracer color for vehicles (minecarts/boats)"))
				.withDesc("Show Vehicle Tracers"),
				new SettingSlider("Thick: ", 0.1, 5, 1.5, 1));
	}

	@Subscribe
	public void onRender(EventWorldRender event) {
		final float thick = (float) getSettings().get(6).asSlider().getValue();

		for (Entity e: mc.world.getEntities()) {
			Vec3d vec = e.getPos();

			Vec3d vec2 = new Vec3d(0, 0, 75).rotateX(-(float) Math.toRadians(mc.cameraEntity.pitch))
					.rotateY(-(float) Math.toRadians(mc.cameraEntity.yaw))
					.add(mc.cameraEntity.getPos().add(0, mc.cameraEntity.getEyeHeight(mc.cameraEntity.getPose()), 0));
			
			float[] col = null;

			if (e instanceof PlayerEntity && e != mc.player && e != mc.cameraEntity && getSettings().get(0).asToggle().state) {
				col = getSettings().get(0).getChild(BleachHack.friendMang.has(e.getName().asString()) ? 1 : 0).asColor().getRGBFloat();
			} else if (e instanceof Monster && getSettings().get(1).asToggle().state) {
				col = getSettings().get(1).getChild(0).asColor().getRGBFloat();
			} else if (EntityUtils.isAnimal(e) && getSettings().get(2).asToggle().state) {
				col = getSettings().get(2).getChild(0).asColor().getRGBFloat();
			} else if (e instanceof ItemEntity && getSettings().get(3).asToggle().state) {
				col = getSettings().get(3).getChild(0).asColor().getRGBFloat();
			} else if (e instanceof EndCrystalEntity && getSettings().get(4).asToggle().state) {
				col = getSettings().get(4).getChild(0).asColor().getRGBFloat();
			} else if ((e instanceof BoatEntity || e instanceof AbstractMinecartEntity) && getSettings().get(5).asToggle().state) {
				col = getSettings().get(5).getChild(0).asColor().getRGBFloat();
			}
			
			if (col != null) {
				RenderUtils.drawLine(vec2.x,vec2.y,vec2.z,vec.x,vec.y,vec.z, col[0], col[1], col[2],thick);
				RenderUtils.drawLine(vec.x,vec.y,vec.z, vec.x,vec.y+(e.getHeight()/1.1),vec.z, col[0], col[1], col[2],thick);
			}
		}
	}
}
