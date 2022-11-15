/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.operation;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public interface OperationBlueprint {

	Operation create(BlockPos origin, Direction dir);
}