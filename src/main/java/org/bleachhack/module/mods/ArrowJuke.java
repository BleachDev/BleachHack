/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.world.WorldUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class ArrowJuke extends Module {

	public ArrowJuke() {
		super("ArrowJuke", KEY_UNBOUND, ModuleCategory.COMBAT, "Tries to dodge arrows coming at you.",
				new SettingMode("Move", "Client", "Packet").withDesc("How to move to avoid the arrow."),
				new SettingSlider("Speed", 0.01, 2, 1, 2).withDesc("The move speed."),
				new SettingSlider("Lookahead", 1, 500, 250, 0).withDesc("How many steps in the future to look ahead."),
				new SettingToggle("Up", false).withDesc("Allows you to move up when dodging the arrow."));
	}

	@BleachSubscribe
	public void onTick(EventTick envent) {
		for (Entity e : mc.world.getEntities()) {
			if (e.age > 75 || !(e instanceof ArrowEntity) || ((ArrowEntity) e).getOwner() == mc.player)
				continue;

			int mode = getSetting(0).asMode().getMode();
			int steps = getSetting(2).asSlider().getValueInt();

			Box playerBox = mc.player.getBoundingBox().expand(0.3);
			List<Box> futureBoxes = new ArrayList<>(steps);

			Box currentBox = e.getBoundingBox();
			Vec3d currentVel = e.getVelocity();

			for (int i = 0; i < steps; i++) {
				currentBox = currentBox.offset(currentVel);
				currentVel = currentVel.multiply(0.99, 0.94, 0.99);
				futureBoxes.add(currentBox);

				if (!mc.world.getOtherEntities(null, currentBox).isEmpty() || WorldUtils.doesBoxCollide(currentBox)) {
					break;
				}
			}

			for (Box box: futureBoxes) {
				if (playerBox.intersects(box)) {
					for (Vec3d vel : getMoveVecs(e.getVelocity())) {
						Box newBox = mc.player.getBoundingBox().offset(vel);

						if (!WorldUtils.doesBoxCollide(newBox) && futureBoxes.stream().noneMatch(playerBox.offset(vel)::intersects)) {
							if (mode == 0 && vel.y == 0) {
								mc.player.setVelocity(vel);
							} else {
								mc.player.updatePosition(mc.player.getX() + vel.x, mc.player.getY() + vel.y, mc.player.getZ() + vel.z);
								mc.player.networkHandler.sendPacket(
										new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false));
							}

							return;
						}
					}
				}
			}
		}
	}

	private List<Vec3d> getMoveVecs(Vec3d arrowVec) {
		double speed = getSetting(1).asSlider().getValue();

		List<Vec3d> list = new ArrayList<>(Arrays.asList(
				arrowVec.subtract(0, arrowVec.y, 0).normalize().multiply(speed).rotateY((float) -Math.toRadians(90f)),
				arrowVec.subtract(0, arrowVec.y, 0).normalize().multiply(speed).rotateY((float) Math.toRadians(90f))));

		Collections.shuffle(list);

		if (getSetting(3).asToggle().getState()) {
			list.add(new Vec3d(0, 2, 0));
		}

		return list;
	}

}
