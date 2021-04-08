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
package bleach.hack.gui.widget;

import bleach.hack.gui.window.Window;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;

public class BleachScrollbar extends AbstractButtonWidget {

	private int totalHeight;
	private int pageHeight;
	private int pageOffset;

	private boolean buttonDown;
	private int lastY;

	public BleachScrollbar(int x, int y, int totalHeight, int pageHeight, int pageOffset) {
		super(x, y, 10, pageHeight, new LiteralText("Scroll bar"));
		this.totalHeight = totalHeight;
		this.pageHeight = pageHeight;
		setPageOffset(pageOffset);
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int[] pos = getCurrentPos();

		if (pos != null && mouseX >= pos[0] && mouseX <= pos[0] + pos[2] && mouseY >= pos[1] && mouseY <= pos[1] + pos[3]) {
			buttonDown = true;
			lastY = (int) mouseY;
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		buttonDown = false;

		return super.mouseClicked(mouseX, mouseY, button);
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		setPageOffset(getPageOffset() + (int) Math.round(-amount * 20));

		return super.mouseScrolled(mouseX, mouseY, amount);
	}

	/* [x, y, w, h] */
	public int[] getCurrentPos() {
		if (totalHeight <= pageHeight) {
			return null;
		}

		int bottom = pageHeight - getScrollbarHeight();

		return new int[] { x, y + (int) (bottom * (pageOffset / (double) (totalHeight - pageHeight))), 10, getScrollbarHeight() };
	}

	private int getScrollbarHeight() {
		if (totalHeight <= pageHeight) {
			return 0;
		}

		return (int) Math.max(25, pageHeight * (pageHeight / (double) totalHeight));
	}

	public int getTotalHeight() {
		return totalHeight;
	}

	public void setTotalHeight(int totalHeight) {
		this.totalHeight = totalHeight;
		setPageOffset(getPageOffset());
	}

	public int getPageHeight() {
		return pageHeight;
	}

	public void setPageHeight(int pageHeight) {
		this.pageHeight = pageHeight;
		setPageOffset(getPageOffset());
	}

	public int getPageOffset() {
		return pageOffset;
	}

	public void setPageOffset(int offset) {
		pageOffset = totalHeight - pageHeight <= 0 ? 0 : MathHelper.clamp(offset, 0, totalHeight - pageHeight);
	}

	public void moveScrollbar(int yDiff) {
		setPageOffset(pageOffset + (int) (yDiff * ((totalHeight - getScrollbarHeight() / 2.2) / (double) (pageHeight - getScrollbarHeight() / 2.2))));
	}

	public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float delta) {
		int[] pos = getCurrentPos();

		//System.out.println(getScrollbarHeight());
		if (pos != null) {
			int color = mouseX >= pos[0] && mouseX <= pos[0] + pos[2] && mouseY >= pos[1] && mouseY <= pos[1] + pos[3] ? 0x906060ff : 0x9040409f;

			Window.fill(matrix, pos[0], pos[1], pos[0] + pos[2], pos[1] + pos[3], color);

			int middleY = pos[1] + (int) (pos[3] / 2d);

			DrawableHelper.fill(matrix, pos[0] + 3, middleY - 3, pos[0] + 7, middleY - 2, 0xff7060ff);
			DrawableHelper.fill(matrix, pos[0] + 3, middleY, pos[0] + 7, middleY + 1, 0xff7060ff);
			DrawableHelper.fill(matrix, pos[0] + 3, middleY + 3, pos[0] + 7, middleY + 4, 0xff7060ff);

			if (buttonDown) {
				moveScrollbar(mouseY - lastY);
				lastY = mouseY;
			}
		}
	}
}
