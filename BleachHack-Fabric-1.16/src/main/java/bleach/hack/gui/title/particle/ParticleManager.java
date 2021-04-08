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

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

public class ParticleManager {

	private List<Particle> particles = new ArrayList<>();

	public void addParticle(int x, int y) {
		particles.add(new Particle(x, y));
	}

	public void renderParticles(MatrixStack matrix) {
		List<Particle> tempParts = new ArrayList<>();

		for (Particle p : particles) {
			p.updateParticles();
			if (p.isDead())
				tempParts.add(p);
		}

		particles.removeAll(tempParts);

		for (Particle p : particles) {
			for (int[] p1 : p.getParticles()) {
				DrawableHelper.fill(matrix, p1[0], p1[1], p1[0] + 1, p1[1] + 1, 0xffffc0e0);
			}
		}
	}
}
