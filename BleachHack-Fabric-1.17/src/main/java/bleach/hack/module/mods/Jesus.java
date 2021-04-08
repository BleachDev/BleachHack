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
