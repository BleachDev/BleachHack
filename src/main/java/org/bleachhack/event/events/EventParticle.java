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

import net.minecraft.client.particle.Particle;
import net.minecraft.particle.ParticleEffect;

public class EventParticle extends Event {

	public static class Normal extends EventParticle {

		private Particle particle;

		public Normal(Particle particle) {
			this.particle = particle;
		}

		public Particle getParticle() {
			return particle;
		}
	}

	public static class Emitter extends EventParticle {

		private ParticleEffect effect;

		public Emitter(ParticleEffect effect) {
			this.effect = effect;
		}

		public ParticleEffect getEffect() {
			return effect;
		}
	}
}
