/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import bleach.hack.eventbus.BleachSubscribe;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingColor;
import bleach.hack.module.setting.base.SettingSlider;
import bleach.hack.module.setting.base.SettingToggle;
import bleach.hack.module.Module;
import bleach.hack.util.render.Renderer;
import bleach.hack.util.render.color.LineColor;
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
		super("Tracers", KEY_UNBOUND, ModuleCategory.RENDER, "Shows lines to entities you select.",
				new SettingToggle("Players", true).withDesc("Shows Tracers for Players.").withChildren(
						new SettingColor("Player Color", 1f, 0.3f, 0.3f, false).withDesc("Tracer color for players."),
						new SettingColor("Friend Color", 0f, 1f, 1f, false).withDesc("Tracer color for friends.")),
				new SettingToggle("Mobs", false).withDesc("Shows Tracers for Mobs.").withChildren(
						new SettingColor("Color", 0.5f, 0.1f, 0.5f, false).withDesc("Tracer color for mobs.")),
				new SettingToggle("Animals", false).withDesc("Shows Tracers for Animals.").withChildren(
						new SettingColor("Color", 0.3f, 1f, 0.3f, false).withDesc("Tracer color for animals.")),
				new SettingToggle("Items", true).withDesc("Shows Tracers for Items.").withChildren(
						new SettingColor("Color", 1f, 0.8f, 0.2f, false).withDesc("Tracer color for items.")),
				new SettingToggle("Crystals", true).withDesc("Shows Tracers for End Crystals.").withChildren(
						new SettingColor("Color", 1f, 0.2f, 1f, false).withDesc("Tracer color for crystals.")),
				new SettingToggle("Vehicles", false).withDesc("Shows Tracers for Vehicles.").withChildren(
						new SettingColor("Color", 0.6f, 0.6f, 0.6f, false).withDesc("Tracer color for vehicles (minecarts/boats).")),

				new SettingSlider("Width", 0.1, 5, 1.5, 1).withDesc("Thickness of the tracers."),
				new SettingSlider("Opacity", 0, 1, 0.75, 2).withDesc("Opacity of the tracers."));
	}

	@BleachSubscribe
	public void onRender(EventWorldRender.Post event) {
		float width = getSetting(6).asSlider().getValueFloat();
		float opacity = getSetting(7).asSlider().getValueFloat();

		for (Entity e : mc.world.getEntities()) {
			Vec3d vec = e.getPos().subtract(Renderer.getInterpolationOffset(e));

			Vec3d vec2 = new Vec3d(0, 0, 75)
					.rotateX(-(float) Math.toRadians(mc.gameRenderer.getCamera().getPitch()))
					.rotateY(-(float) Math.toRadians(mc.gameRenderer.getCamera().getYaw()))
					.add(mc.cameraEntity.getEyePos());

			float[] col = null;

			if (e instanceof PlayerEntity && e != mc.player && e != mc.cameraEntity && getSetting(0).asToggle().state) {
				col = getSetting(0).asToggle().getChild(BleachHack.friendMang.has(e) ? 1 : 0).asColor().getRGBFloat();
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
				Renderer.drawLine(vec2.x, vec2.y, vec2.z, vec.x, vec.y, vec.z, LineColor.single(col[0], col[1], col[2], opacity), width);
				Renderer.drawLine(vec.x, vec.y, vec.z, vec.x, vec.y + e.getHeight() * 0.9, vec.z, LineColor.single(col[0], col[1], col[2], opacity), width);
			}
		}
	}
}
