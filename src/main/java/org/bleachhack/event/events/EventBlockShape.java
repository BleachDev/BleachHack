/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.event.events;

import org.bleachhack.event.Event;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;

public class EventBlockShape extends Event {

	private BlockState state;
	private BlockPos pos;
	private VoxelShape shape;

	public EventBlockShape(BlockState state, BlockPos pos, VoxelShape shape) {
		this.state = state;
		this.pos = pos;
		this.setShape(shape);
	}

	public BlockState getState() {
		return state;
	}

	public void setState(BlockState state) {
		this.state = state;
	}

	public BlockPos getPos() {
		return pos;
	}

	public void setPos(BlockPos pos) {
		this.pos = pos;
	}

	public VoxelShape getShape() {
		return shape;
	}

	public void setShape(VoxelShape shape) {
		this.shape = shape;
	}
}
