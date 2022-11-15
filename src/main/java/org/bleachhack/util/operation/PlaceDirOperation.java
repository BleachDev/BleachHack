/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.operation;

import org.apache.commons.lang3.ArrayUtils;
import org.bleachhack.util.InventoryUtils;
import org.bleachhack.util.world.WorldUtils;

import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;

/**
 * THIS DOES NOT PLACE A BLOCK ON A SPECIFIC SIDE OF A BLOCK!!
 * This faces you in a direction then places a block, useful for directional blocks like pistons
 */
public class PlaceDirOperation extends PlaceOperation {

	private Direction dir;
	private boolean faced;

	protected PlaceDirOperation(BlockPos pos, Direction dir, Item... items) {
		super(pos, items);
		this.dir = dir;
	}

	public static OperationBlueprint blueprint(int localX, int localY, int localZ, Direction localDir, Item... items) {
		int horizontal = (localDir.getHorizontal() + 1) % 4;
		return (origin, dir) -> new PlaceDirOperation(origin.add(rotate(localX, localY, localZ, dir)),
				localDir.getAxis() == Axis.Y ? localDir : Direction.fromHorizontal(Math.floorMod(dir.getHorizontal() - horizontal, 4)), items);
	}

	@Override
	public boolean execute() {
		if (faced) {
			int slot = InventoryUtils.getSlot(true, i -> ArrayUtils.contains(items, mc.player.getInventory().getStack(i).getItem()));

			faced = false;
			return slot != -1 && WorldUtils.placeBlock(pos, slot, 0, false, false, true);
		} else {
			Vec3d lookPos = mc.player.getEyePos().add(dir.getOffsetX(), dir.getOffsetY(), dir.getOffsetZ());
			WorldUtils.facePosPacket(lookPos.getX(), lookPos.getY(), lookPos.getZ());

			faced = true;
			return false;
		}
	}
}
