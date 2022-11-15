/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.gui.clickgui.window;

import org.bleachhack.gui.window.Window;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;

public abstract class ClickGuiWindow extends Window {

	protected MinecraftClient mc = MinecraftClient.getInstance();

	public int mouseX;
	public int mouseY;

	public boolean hiding;

	public int keyDown = -1;
	public boolean lmDown = false;
	public boolean rmDown = false;
	public boolean lmHeld = false;
	public int mwScroll = 0;

	public ClickGuiWindow(int x1, int y1, int x2, int y2, String title, ItemStack icon) {
		super(x1, y1, x2, y2, title, icon);
	}

	public boolean shouldClose(int mouseX, int mouseY) {
		return false;
	}

	protected void drawBackground(MatrixStack matrices, int mouseX, int mouseY, TextRenderer textRend) {
		/* background */
		DrawableHelper.fill(matrices, x1, y1 + 1, x1 + 1, y2 - 1, 0xff6060b0);
		horizontalGradient(matrices, x1 + 1, y1, x2 - 1, y1 + 1, 0xff6060b0, 0xff8070b0);
		DrawableHelper.fill(matrices, x2 - 1, y1 + 1, x2, y2 - 1, 0xff8070b0);
		horizontalGradient(matrices, x1 + 1, y2 - 1, x2 - 1, y2, 0xff6060b0, 0xff8070b0);

		DrawableHelper.fill(matrices, x1 + 1, y1 + 12, x2 - 1, y2 - 1, 0x90606090);

		/* title bar */
		horizontalGradient(matrices, x1 + 1, y1 + 1, x2 - 1, y1 + 12, 0xff6060b0, 0xff8070b0);

		/* +/- text */
		textRend.draw(matrices, hiding ? "+" : "_", x2 - 10, y1 + (hiding ? 4 : 2), 0x000000);
		textRend.draw(matrices, hiding ? "+" : "_", x2 - 11, y1 + (hiding ? 3 : 1), 0xffffff);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY) {
		super.render(matrices, mouseX, mouseY);

		if (rmDown && mouseOver(x1, y1, x1 + (x2 - x1), y1 + 13)) {
			mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			hiding = !hiding;
		}
	}
	
	public Tooltip getTooltip() {
		return null;
	}

	public boolean mouseOver(int minX, int minY, int maxX, int maxY) {
		return mouseX >= minX && mouseX <= maxX && mouseY >= minY && mouseY < maxY;
	}

	public void updateKeys(int mouseX, int mouseY, int keyDown, boolean lmDown, boolean rmDown, boolean lmHeld, int mwScroll) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.keyDown = keyDown;
		this.lmDown = lmDown;
		this.rmDown = rmDown;
		this.lmHeld = lmHeld;
		this.mwScroll = mwScroll;
	}
	
	public static class Tooltip {
		
		public final int x;
		public final int y;
		public final String text;
		
		public Tooltip(int x, int y, String text) {
			this.x = x;
			this.y = y;
			this.text = text;
		}
	}
}
