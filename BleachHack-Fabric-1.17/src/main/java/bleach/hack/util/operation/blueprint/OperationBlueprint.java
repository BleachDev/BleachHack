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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class OperationBlueprint {

	/* popbob penis
	 *
	 *   |    *
	 *   |    *
	 *   |   * *
	 *  x|0,0______
	 *    z
	 */
	protected int localX;
	protected int localY;
	protected int localZ;

	public abstract Operation create(BlockPos pos, Direction dir);
}