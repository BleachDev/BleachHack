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

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public class EventBiomeColor extends Event {

	protected BlockRenderView world;
	protected BlockPos pos;
	protected int color;

	public static class Grass extends EventBiomeColor {

		public Grass(BlockRenderView world, BlockPos pos, int color) {
			this.world = world;
			this.pos = pos;
			this.color = color;
		}

	}

	public static class Foilage extends EventBiomeColor {

		public Foilage(BlockRenderView world, BlockPos pos, int color) {
			this.world = world;
			this.pos = pos;
			this.color = color;
		}

	}

	public static class Water extends EventBiomeColor {

		public Water(BlockRenderView world, BlockPos pos, int color) {
			this.world = world;
			this.pos = pos;
			this.color = color;
		}

	}

	public BlockRenderView getWorld() {
		return world;
	}

	public void setWorld(BlockRenderView world) {
		this.world = world;
	}

	public BlockPos getPos() {
		return pos;
	}

	public void setPos(BlockPos pos) {
		this.pos = pos;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

}
