/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.event.events;

import net.minecraft.client.gl.PostEffectProcessor;
import org.bleachhack.event.Event;

public class EventRenderShader extends Event {
	
	private PostEffectProcessor effect;
	
	public EventRenderShader(PostEffectProcessor effect) {
		this.setEffect(effect);
	}

	public PostEffectProcessor getEffect() {
		return effect;
	}

	public void setEffect(PostEffectProcessor effect) {
		this.effect = effect;
	}

}
