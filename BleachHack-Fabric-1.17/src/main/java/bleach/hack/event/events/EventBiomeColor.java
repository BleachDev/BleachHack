/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public class EventBiomeColor extends Event {

	protected BlockRenderView world;
	protected BlockPos pos;

	private Integer color = null;

	public static class Grass extends EventBiomeColor {

		public Grass(BlockRenderView world, BlockPos pos) {
			this.world = world;
			this.pos = pos;
		}

	}

	public static class Foilage extends EventBiomeColor {

		public Foilage(BlockRenderView world, BlockPos pos) {
			this.world = world;
			this.pos = pos;
		}

	}

	public static class Water extends EventBiomeColor {

		public Water(BlockRenderView world, BlockPos pos) {
			this.world = world;
			this.pos = pos;
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
	
	public void setColor(Integer color) {
		this.color = color;
	}

	public Integer getColor() {
		return color;
	}

}
