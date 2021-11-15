/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.operation;

import org.bleachhack.util.InventoryUtils;
import org.bleachhack.util.world.WorldUtils;

import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * THIS DOES NOT PLACE A BLOCK ON A SPECIFIC SIDE OF A BLOCK!!
 * This faces you in a direction then places a block, useful for directional blocks like pistons
 */
public class PlaceDirOperation extends PlaceOperation {

	private Direction dir;
	private boolean faced = false;

	public PlaceDirOperation(BlockPos pos, Item item, Direction dir) {
		super(pos, item);
		this.dir = dir;
	}

	@Override
	public boolean execute() {
		int slot = InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == item);

		if (slot != -1 && WorldUtils.canPlaceBlock(pos)) {
			if (!faced) {
				Vec3d lookPos = mc.player.getEyePos().add(dir.getOffsetX(), dir.getOffsetY(), dir.getOffsetZ());
				WorldUtils.facePosPacket(lookPos.getX(), lookPos.getY(), lookPos.getZ());

				faced = true;
				return false;
			}

			return WorldUtils.placeBlock(pos, slot, 0, false, false, true);
		}

		return false;
	}

	@Override
	public boolean verify() {
		return true;
	}
}
