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

import java.util.HashSet;
import java.util.Set;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Ghosthand extends Module {

	public Ghosthand() {
		super("Ghosthand", KEY_UNBOUND, Category.PLAYER, "Opens Containers Through Walls");
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (!mc.options.keyUse.isPressed() || mc.player.isSneaking())
			return;

		// Return if we are looking at any block entities
		BlockPos lookingPos = new BlockPos(mc.player.raycast(4.25, mc.getTickDelta(), false).getPos());
		for (BlockEntity b : WorldUtils.getBlockEntities()) {
			if (lookingPos.equals(b.getPos())) {
				return;
			}
		}

		Set<BlockPos> posList = new HashSet<>();

		Vec3d nextPos = new Vec3d(0, 0, 0.1)
				.rotateX(-(float) Math.toRadians(mc.player.pitch))
				.rotateY(-(float) Math.toRadians(mc.player.yaw));

		for (int i = 1; i < 50; i++) {
			BlockPos curPos = new BlockPos(mc.player.getCameraPosVec(mc.getTickDelta()).add(nextPos.multiply(i)));
			if (!posList.contains(curPos)) {
				posList.add(curPos);
	
				for (BlockEntity b : WorldUtils.getBlockEntities()) {
					if (b.getPos().equals(curPos)) {
						mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
								new BlockHitResult(mc.player.getPos(), Direction.UP, curPos, true));
						return;
					}
				}
			}
		}
	}

}
