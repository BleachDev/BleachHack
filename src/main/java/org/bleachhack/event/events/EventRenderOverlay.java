/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.event.events;

import net.minecraft.client.util.math.MatrixStack;
import org.bleachhack.event.Event;

import net.minecraft.util.Identifier;

public class EventRenderOverlay extends Event {

	private MatrixStack matrices;
	private Identifier texture;
	private float opacity;
	
	public EventRenderOverlay(MatrixStack matrices, Identifier texture, float opacity) {
		this.matrices = matrices;
		this.texture = texture;
		this.opacity = opacity;
	}

	public MatrixStack getMatrices() {
		return matrices;
	}

	public Identifier getTexture() {
		return texture;
	}

	public float getOpacity() {
		return opacity;
	}

}
