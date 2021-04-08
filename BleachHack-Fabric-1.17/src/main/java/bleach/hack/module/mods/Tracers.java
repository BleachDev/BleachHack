/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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

import com.google.common.eventbus.Subscribe;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.RenderUtils;
import bleach.hack.util.world.EntityUtils;
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
				new SettingToggle("Players", true).withDesc("Show Player Tracers").withChildren(
						new SettingColor("Player Color", 1f, 0.3f, 0.3f, false).withDesc("Tracer color for players"),
						new SettingColor("Friend Color", 0f, 1f, 1f, false).withDesc("Tracer color for friends")),
				new SettingToggle("Mobs", false).withDesc("Show Mob Tracers").withChildren(
						new SettingColor("Color", 0.5f, 0.1f, 0.5f, false).withDesc("Tracer color for mobs")),
				new SettingToggle("Animals", false).withDesc("Show Animal Tracers").withChildren(
						new SettingColor("Color", 0.3f, 1f, 0.3f, false).withDesc("Tracer color for animals")),
				new SettingToggle("Items", true).withDesc("Show Item Tracers").withChildren(
						new SettingColor("Color", 1f, 0.8f, 0.2f, false).withDesc("Tracer color for items")),
				new SettingToggle("Crystals", true).withDesc("Show End Crystal Tracers").withChildren(
						new SettingColor("Color", 1f, 0.2f, 1f, false).withDesc("Tracer color for crystals")),
				new SettingToggle("Vehicles", false).withDesc("Show Vehicle Tracers").withChildren(
						new SettingColor("Color", 0.6f, 0.6f, 0.6f, false).withDesc("Tracer color for vehicles (minecarts/boats)")),

				new SettingSlider("Width", 0.1, 5, 1.5, 1).withDesc("Thickness of the tracers"),
				new SettingSlider("Opacity", 0, 1, 0.75, 2).withDesc("Opacity of the tracers"));
	}

	@Subscribe
	public void onRender(EventWorldRender.Post event) {
		float width = (float) getSetting(6).asSlider().getValue();
		float opacity = (float) getSetting(7).asSlider().getValue();

		for (Entity e : mc.world.getEntities()) {
			Vec3d vec = e.getPos();

			Vec3d vec2 = new Vec3d(0, 0, 75).rotateX(-(float) Math.toRadians(mc.gameRenderer.getCamera().getPitch()))
					.rotateY(-(float) Math.toRadians(mc.gameRenderer.getCamera().getYaw()))
					.add(mc.cameraEntity.getPos().add(0, mc.cameraEntity.getEyeHeight(mc.cameraEntity.getPose()), 0));

			float[] col = null;

			if (e instanceof PlayerEntity && e != mc.player && e != mc.cameraEntity && getSetting(0).asToggle().state) {
				col = getSetting(0).asToggle().getChild(BleachHack.friendMang.has(e.getName().getString()) ? 1 : 0).asColor().getRGBFloat();
			} else if (e instanceof Monster && getSetting(1).asToggle().state) {
				col = getSetting(1).asToggle().getChild(0).asColor().getRGBFloat();
			} else if (EntityUtils.isAnimal(e) && getSetting(2).asToggle().state) {
				col = getSetting(2).asToggle().getChild(0).asColor().getRGBFloat();
			} else if (e instanceof ItemEntity && getSetting(3).asToggle().state) {
				col = getSetting(3).asToggle().getChild(0).asColor().getRGBFloat();
			} else if (e instanceof EndCrystalEntity && getSetting(4).asToggle().state) {
				col = getSetting(4).asToggle().getChild(0).asColor().getRGBFloat();
			} else if ((e instanceof BoatEntity || e instanceof AbstractMinecartEntity) && getSetting(5).asToggle().state) {
				col = getSetting(5).asToggle().getChild(0).asColor().getRGBFloat();
			}

			if (col != null) {
				RenderUtils.drawLine(vec2.x, vec2.y, vec2.z, vec.x, vec.y, vec.z, col[0], col[1], col[2], opacity, width);
				RenderUtils.drawLine(vec.x, vec.y, vec.z, vec.x, vec.y + e.getHeight() * 0.9, vec.z, col[0], col[1], col[2], opacity, width);
			}
		}
	}
}
