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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;

public class EventDrawContainer extends Event {

	private HandledScreen<?> screen;
	public int mouseX;
	public int mouseY;
	public MatrixStack matrix;

	public EventDrawContainer(HandledScreen<?> screen, int mouseX, int mouseY, MatrixStack matrix) {
		this.screen = screen;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.matrix = matrix;
	}

	public Screen getScreen() {
		return screen;
	}
}
