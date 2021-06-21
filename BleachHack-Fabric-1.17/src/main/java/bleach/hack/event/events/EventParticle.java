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
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.BlockPos;

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

	public static class BlockBreaking extends EventParticle {

		private BlockPos pos;
		private BlockState state;

		public BlockBreaking(BlockPos pos, BlockState state) {
			this.pos = pos;
			this.state = state;
		}

		public BlockPos getPos() {
			return pos;
		}

		public BlockState getState() {
			return state;
		}
	}
}
