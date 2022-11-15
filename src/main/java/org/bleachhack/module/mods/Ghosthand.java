/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import java.util.HashSet;
import java.util.Set;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.util.world.WorldUtils;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Ghosthand extends Module {

	public Ghosthand() {
		super("Ghosthand", KEY_UNBOUND, ModuleCategory.PLAYER, "Opens containers through walls.");
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (!mc.options.useKey.isPressed() || mc.player.isSneaking())
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
				.rotateX(-(float) Math.toRadians(mc.player.getPitch()))
				.rotateY(-(float) Math.toRadians(mc.player.getYaw()));

		for (int i = 1; i < 50; i++) {
			BlockPos curPos = new BlockPos(mc.player.getCameraPosVec(mc.getTickDelta()).add(nextPos.multiply(i)));
			if (!posList.contains(curPos)) {
				posList.add(curPos);
	
				for (BlockEntity b : WorldUtils.getBlockEntities()) {
					if (b.getPos().equals(curPos)) {
						mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
								new BlockHitResult(Vec3d.ofCenter(curPos, 1), Direction.UP, curPos, true));
						return;
					}
				}
			}
		}
	}

}
