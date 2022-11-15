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

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;

public class EventRenderFluid extends Event {

	private FluidState state;
	private BlockPos pos;
	private VertexConsumer vertexConsumer;

	public EventRenderFluid(FluidState state, BlockPos pos, VertexConsumer vertexConsumer) {
		this.state = state;
		this.pos = pos;
		this.vertexConsumer = vertexConsumer;
	}

	public FluidState getState() {
		return state;
	}
	
	public BlockPos getPos() {
		return pos;
	}

	public VertexConsumer getVertexConsumer() {
		return vertexConsumer;
	}
}
