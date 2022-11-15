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

public class EventPlayerPushed extends Event {
	
	private double pushX;
	private double pushY;
	private double pushZ;

	public EventPlayerPushed(double pushX, double pushY, double pushZ) {
		this.pushX = pushX;
		this.pushY = pushY;
		this.pushZ = pushZ;
	}

	public double getPushX() {
		return pushX;
	}

	public void setPushX(double pushX) {
		this.pushX = pushX;
	}

	public double getPushY() {
		return pushY;
	}

	public void setPushY(double pushY) {
		this.pushY = pushY;
	}

	public double getPushZ() {
		return pushZ;
	}

	public void setPushZ(double pushZ) {
		this.pushZ = pushZ;
	}
}
