/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.render.color;

public abstract class RenderColor {

	protected Integer[] overwriteColor = new Integer[4];

	public void overwriteRed(Integer red) {
		overwriteColor[0] = red;
	}

	public void overwriteGreen(Integer green) {
		overwriteColor[1] = green;
	}

	public void overwriteBlue(Integer blue) {
		overwriteColor[2] = blue;
	}

	public void overwriteAlpha(Integer alpha) {
		overwriteColor[3] = alpha;
	}

	protected void cloneOverwriteTo(RenderColor otherColor) {
		otherColor.overwriteColor = overwriteColor.clone();
	}

}
