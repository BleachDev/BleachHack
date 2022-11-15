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

import net.minecraft.entity.damage.DamageSource;

public class EventDamage extends Event {

	public static class Normal extends EventDamage {

		private DamageSource source;
		private float amount;

		public Normal(DamageSource source, float amount) {
			this.source = source;
			this.amount = amount;
		}

		public DamageSource getSource() {
			return source;
		}

		public float getAmount() {
			return amount;
		}
	}

	public static class Knockback extends EventDamage {

		private double velX;
		private double velY;
		private double velZ;

		public Knockback(double velX, double velY, double velZ) {
			this.velX = velX;
			this.velY = velY;
			this.velZ = velZ;
		}

		public double getVelX() {
			return velX;
		}

		public void setVelX(double velX) {
			this.velX = velX;
		}

		public double getVelY() {
			return velY;
		}

		public void setVelY(double velY) {
			this.velY = velY;
		}

		public double getVelZ() {
			return velZ;
		}

		public void setVelZ(double velZ) {
			this.velZ = velZ;
		}
	}
}
