/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util.operation.blueprint;

import bleach.hack.util.operation.Operation;
import bleach.hack.util.operation.PlaceDirOperation;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class PlaceDirOperationBlueprint extends PlaceOperationBlueprint {

	private Direction rotDir;

	public PlaceDirOperationBlueprint(int localX, int localY, int localZ, Item item, Direction localDir) {
		super(localX, localY, localZ, item);
		this.rotDir = localDir;
	}

	@Override
	public Operation create(BlockPos pos, Direction dir) {
		return new PlaceDirOperation(pos.add(
				(dir == Direction.EAST ? localX : dir == Direction.WEST ? -localX : dir == Direction.SOUTH ? -localZ : localZ),
				localY,
				(dir == Direction.EAST ? localZ : dir == Direction.WEST ? -localZ : dir == Direction.SOUTH ? localX : -localX)), item,
				rotDir.getAxis() == Axis.Y ? rotDir : rotDir == Direction.EAST ? dir : rotDir == Direction.SOUTH ? dir.rotateYClockwise()
						: rotDir == Direction.WEST ? dir.getOpposite() : dir.rotateYCounterclockwise());
	}
}