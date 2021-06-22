/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.gui.clickgui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Triple;
import bleach.hack.gui.window.WindowScreen;
import bleach.hack.gui.clickgui.window.ClickGuiWindow;
import bleach.hack.gui.window.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public abstract class ClickGuiScreen extends WindowScreen {

	protected int keyDown = -1;
	protected boolean lmDown = false;
	protected boolean rmDown = false;
	protected boolean lmHeld = false;
	protected int mwScroll = 0;

	public ClickGuiScreen(Text title) {
		super(title);
	}

	public boolean isPauseScreen() {
		return false;
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);

		for (Window w : getWindows()) {
			if (w instanceof ClickGuiWindow) {
				((ClickGuiWindow) w).updateKeys(mouseX, mouseY, keyDown, lmDown, rmDown, lmHeld, mwScroll);
			}
		}

		super.render(matrices, mouseX, mouseY, delta);

		matrices.push();
		matrices.translate(0, 0, 250);

		for (Window w : getWindows()) {
			if (w instanceof ClickGuiWindow) {
				Triple<Integer, Integer, String> tooltip = ((ClickGuiWindow) w).getTooltip();

				if (tooltip != null) {
					int tooltipY = tooltip.getMiddle();

					String[] split = tooltip.getRight().split("\n", -1 /* Adding -1 makes it keep empty splits */);
					ArrayUtils.reverse(split);
					for (String s: split) {
						/* Match lines to end of words after it reaches 22 characters long */
						Matcher mat = Pattern.compile(".{1,22}\\b\\W*").matcher(s);

						List<String> lines = new ArrayList<>();

						while (mat.find())
							lines.add(mat.group().trim());

						if (lines.isEmpty())
							lines.add(s);

						int start = tooltipY - lines.size() * 10;
						for (int l = 0; l < lines.size(); l++) {
							fill(matrices, tooltip.getLeft(), start + (l * 10) - 1,
									tooltip.getLeft() + textRenderer.getWidth(lines.get(l)) + 3,
									start + (l * 10) + 9, 0xff000000);

							textRenderer.drawWithShadow(matrices, lines.get(l), tooltip.getLeft() + 2, start + (l * 10), -1);
						}

						tooltipY -= lines.size() * 10;
					}
				}
			}
		}

		matrices.pop();

		lmDown = false;
		rmDown = false;
		keyDown = -1;
		mwScroll = 0;
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			lmDown = true;
			lmHeld = true;
		} else if (button == 1) {
			rmDown = true;
		}

		// Fix having to double click windows to move them
		for (Window w : getWindows()) {
			if (mouseX > w.x1 && mouseX < w.x2 && mouseY > w.y1 && mouseY < w.y2 && !w.closed) {
				w.mouseClicked(mouseX, mouseY, button);
				break;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0)
			lmHeld = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		keyDown = keyCode;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		mwScroll = (int) amount;
		return super.mouseScrolled(mouseX, mouseY, amount);
	}
}
