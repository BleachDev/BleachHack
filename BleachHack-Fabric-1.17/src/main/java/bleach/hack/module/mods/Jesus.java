/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import org.lwjgl.glfw.GLFW;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class Jesus extends Module {

	public Jesus() {
		super("Jesus", GLFW.GLFW_KEY_J, Category.PLAYER, "Allows you to walk on water");
	}

	@Subscribe
	public void onTick(EventTick event) {
		Entity e = mc.player.getVehicle() != null ? mc.player.getVehicle() : mc.player;

		if (e.isSneaking() || e.fallDistance > 3f)
			return;

		if (WorldUtils.isFluid(new BlockPos(e.getPos().add(0, 0.3, 0)))) {
			e.setVelocity(e.getVelocity().x, 0.08, e.getVelocity().z);
		} else if (WorldUtils.isFluid(new BlockPos(e.getPos().add(0, 0.1, 0)))) {
			e.setVelocity(e.getVelocity().x, 0.05, e.getVelocity().z);
		} else if (WorldUtils.isFluid(new BlockPos(e.getPos().add(0, 0.05, 0)))) {
			e.setVelocity(e.getVelocity().x, 0.01, e.getVelocity().z);
		} else if (WorldUtils.isFluid(new BlockPos(e.getPos()))) {
			e.setVelocity(e.getVelocity().x, -0.005, e.getVelocity().z);
			e.setOnGround(true);
		}
	}
}
