/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.event.events;

import java.util.List;

import bleach.hack.event.Event;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class EventRenderTooltip extends Event {

	private Screen screen;
	private MatrixStack matrices;
	private List<Text> text;
	private int x;
	private int y;
	private int mouseX;
	private int mouseY;

	public EventRenderTooltip(Screen screen, MatrixStack matrices, List<Text> text, int x, int y, int mouseX, int mouseY) {
		this.matrices = matrices;
		this.screen = screen;
		this.text = text;
		this.x = x;
		this.y = y;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}
	
	public Screen getScreen() {
		return screen;
	}

	public MatrixStack getMatrix() {
		return matrices;
	}

	public void setMatrix(MatrixStack matrices) {
		this.matrices = matrices;
	}

	public List<Text> getText() {
		return text;
	}

	public void setText(List<Text> text) {
		this.text = text;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getMouseX() {
		return mouseX;
	}

	public int getMouseY() {
		return mouseY;
	}
	
}
