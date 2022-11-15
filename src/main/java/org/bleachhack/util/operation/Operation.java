/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.operation;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class Operation {

	protected static final MinecraftClient mc = MinecraftClient.getInstance();

	public BlockPos pos;
	public abstract boolean canExecute();
	public abstract boolean execute();
	public abstract boolean verify();

	public abstract void render();

	// Rotation helper method to help blueprints
	protected static BlockPos rotate(int localX, int localY, int localZ, Direction dir) {
		return switch (dir) {
			case SOUTH -> new BlockPos(-localZ, localY, localX);
			case WEST -> new BlockPos(-localX, localY, -localZ);
			case NORTH -> new BlockPos(localZ, localY, -localX);
			default -> new BlockPos(localX, localY, localZ);
		};
	}
}
