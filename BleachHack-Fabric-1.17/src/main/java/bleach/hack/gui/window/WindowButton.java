/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.gui.window;

public class WindowButton {

	public int x1;
	public int y1;
	public int x2;
	public int y2;
	public String text;
	public Runnable action;

	public WindowButton(int x1, int y1, int x2, int y2, String text, Runnable action) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.text = text;
		this.action = action;
	}

}
