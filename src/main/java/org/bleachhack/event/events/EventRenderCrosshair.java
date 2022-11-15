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

import net.minecraft.client.util.math.MatrixStack;

public class EventRenderCrosshair extends Event {

	private MatrixStack matrices;

	public EventRenderCrosshair(MatrixStack matrices) {
		this.setMatrices(matrices);
	}

	public MatrixStack getMatrices() {
		return matrices;
	}

	public void setMatrices(MatrixStack matrices) {
		this.matrices = matrices;
	}
}
