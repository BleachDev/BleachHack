/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import java.util.HashSet;
import java.util.Set;

import bleach.hack.eventbus.BleachSubscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.Module;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Ghosthand extends Module {

	public Ghosthand() {
		super("Ghosthand", KEY_UNBOUND, ModuleCategory.PLAYER, "Opens Containers Through Walls");
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (!mc.options.keyUse.isPressed() || mc.player.isSneaking())
			return;

		// Return if we are looking at any block entities
		BlockPos lookingPos = new BlockPos(mc.player.raycast(4.25, mc.getTickDelta(), false).getPos());
		for (BlockEntity b : mc.world.blockEntities) {
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
	
				for (BlockEntity b : mc.world.blockEntities) {
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
