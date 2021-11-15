/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventBlockShape;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.module.setting.base.SettingMode;
import org.bleachhack.util.world.WorldUtils;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShapes;

public class Jesus extends Module {

	public Jesus() {
		super("Jesus", KEY_UNBOUND, ModuleCategory.PLAYER, "Allows you to walk on water.",
				new SettingMode("Mode", "Vibrate", "Solid").withDesc("The jesus mode."));
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		Entity e = mc.player.getRootVehicle();

		if (e.isSneaking() || e.fallDistance > 3f) 
			return;

		if (WorldUtils.isFluid(new BlockPos(e.getPos().add(0, 0.3, 0)))) {
			e.setVelocity(e.getVelocity().x, 0.08, e.getVelocity().z);
		} else if (WorldUtils.isFluid(new BlockPos(e.getPos().add(0, 0.1, 0)))) {
			e.setVelocity(e.getVelocity().x, 0.05, e.getVelocity().z);
		} else if (WorldUtils.isFluid(new BlockPos(e.getPos().add(0, 0.05, 0)))) {
			e.setVelocity(e.getVelocity().x, 0.01, e.getVelocity().z);
		} else if (WorldUtils.isFluid(e.getBlockPos())) {
			e.setVelocity(e.getVelocity().x, -0.005, e.getVelocity().z);
			e.setOnGround(true);
		}
	}

	@BleachSubscribe
	public void onBlockShape(EventBlockShape event) {
		if (getSetting(0).asMode().mode == 1
				&& WorldUtils.isFluid(event.getPos())
				&& !mc.player.isSneaking()
				&& !mc.player.isTouchingWater()
				&& mc.player.getY() >= event.getPos().getY() + 0.9) {
			event.setShape(VoxelShapes.cuboid(0, 0, 0, 1, 0.9, 1));
		}
	}
}
