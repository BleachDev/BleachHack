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

import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.math.Vec3d;

public class EventSkyRender extends Event {

	public static class Properties extends EventSkyRender {

		private DimensionEffects sky;

		public Properties(DimensionEffects sky) {
			this.setSky(sky);
		}

		public DimensionEffects getSky() {
			return sky;
		}

		public void setSky(DimensionEffects sky) {
			this.sky = sky;
		}
	}

	public static class Color extends EventSkyRender {

		private float tickDelta;
		private Vec3d color = null;

		public Color(float tickDelta) {
			this.tickDelta = tickDelta;
		}

		public float getTickDelta() {
			return tickDelta;
		}

		public void setColor(Vec3d color) {
			this.color = color;
		}

		public Vec3d getColor() {
			return color;
		}

		public static class SkyColor extends Color {

			public SkyColor(float tickDelta) {
				super(tickDelta);
			}
		}

		public static class CloudColor extends Color {

			public CloudColor(float tickDelta) {
				super(tickDelta);
			}
		}

		public static class FogColor extends Color {

			public FogColor(float tickDelta) {
				super(tickDelta);
			}
		}

		public static class EndSkyColor extends Color {

			public EndSkyColor(float tickDelta) {
				super(tickDelta);
			}
		}
	}
}
