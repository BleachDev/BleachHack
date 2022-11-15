/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.gui.effect;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

public class ParticleManager {

	private List<Particle> particles = new ArrayList<>();

	public void addParticle(int x, int y) {
		particles.add(new Particle(x, y));
	}

	public void renderParticles(MatrixStack matrices) {
		List<Particle> tempParts = new ArrayList<>();

		for (Particle p : particles) {
			p.updateParticles();
			if (p.isDead())
				tempParts.add(p);
		}

		particles.removeAll(tempParts);

		for (Particle p : particles) {
			for (int[] p1 : p.getParticles()) {
				DrawableHelper.fill(matrices, p1[0], p1[1], p1[0] + 1, p1[1] + 1, 0xffffc0e0);
			}
		}
	}
}
