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

import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;

public class EventClientMove extends Event {

	private MovementType type;
	private Vec3d vec;

	public EventClientMove(MovementType type, Vec3d vec) {
		this.type = type;
		this.vec = vec;
	}

	public MovementType getType() {
		return type;
	}

	public void setType(MovementType type) {
		this.type = type;
	}

	public Vec3d getVec() {
		return vec;
	}

	public void setVec(Vec3d vec) {
		this.vec = vec;
	}


}
