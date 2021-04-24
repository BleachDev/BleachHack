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
import bleach.hack.util.operation.RemoveOperation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class RemoveOperationBlueprint extends OperationBlueprint {

	public RemoveOperationBlueprint(int localX, int localY, int localZ) {
		this.localX = localX;
		this.localY = localY;
		this.localZ = localZ;
	}

	@Override
	public Operation create(BlockPos pos, Direction dir) {
		return new RemoveOperation(pos.add(
				(dir == Direction.EAST ? localX : dir == Direction.WEST ? -localX : dir == Direction.SOUTH ? -localZ : localZ),
				localY,
				(dir == Direction.EAST ? localZ : dir == Direction.WEST ? -localZ : dir == Direction.SOUTH ? localX : -localX)));
	}
}