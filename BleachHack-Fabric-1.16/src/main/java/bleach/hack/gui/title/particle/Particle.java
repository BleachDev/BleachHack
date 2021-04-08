/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2019 Bleach.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.gui.title.particle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Particle {

	private Random rand = new Random();
	private int tick = 0;
	private int x;
	private int y;
	private int lifespan;
	private boolean dead = false;
	private long lastTick;

	private List<int[]> particles = new ArrayList<>();

	public Particle(int x, int y) {
		this.x = x;
		this.y = y;
		genParticles();
		lastTick = System.currentTimeMillis();
		lifespan = rand.nextInt(20);
	}

	public void genParticles() {
		for (int j = 0; j < rand.nextInt(10); j++) {
			particles.add(new int[] { x + rand.nextInt(5) - 2, y + rand.nextInt(5) - 2 });
		}
	}

	public void updateParticles() {
		/* This is here to make it only trigger if a "tick" has passed */
		if (System.currentTimeMillis() < lastTick + 16)
			return;
		lastTick = System.currentTimeMillis();

		tick++;

		if (tick > lifespan) {
			dead = true;
			particles.clear();
		}

		for (int i = 0; i < particles.size() - 1; i++) {
			int[] pos = particles.get(i);
			int diffx = pos[0] - x;
			int diffy = pos[1] - y;

			particles.set(i, new int[] { pos[0] + (diffx / tick), pos[1] + (diffy / tick) });
		}
	}

	public List<int[]> getParticles() {
		return particles;
	}

	public boolean isDead() {
		return dead;
	}
}
