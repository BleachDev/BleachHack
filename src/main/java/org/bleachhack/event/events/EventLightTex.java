/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.event.events;

import net.minecraft.world.dimension.DimensionType;
import org.bleachhack.event.Event;

public class EventLightTex extends Event {

	public static class Brightness extends EventLightTex {

		private DimensionType dimension;
		private int lightLevel;
		private float brightness;

		public Brightness(DimensionType dimension, int lightLevel, float brightness) {
			this.dimension = dimension;
			this.lightLevel = lightLevel;
			this.brightness = brightness;
		}

		public DimensionType getDimension() {
			return dimension;
		}

		public int getLightLevel() {
			return lightLevel;
		}

		public float getBrightness() {
			return brightness;
		}

		public void setBrightness(float brightness) {
			this.brightness = brightness;
		}
	}

	public static class Gamma extends EventLightTex {

		private float gamma;

		public Gamma(float gamma) {
			this.gamma = gamma;
		}

		public float getGamma() {
			return gamma;
		}

		public void setGamma(float gamma) {
			this.gamma = gamma;
		}
	}
}
