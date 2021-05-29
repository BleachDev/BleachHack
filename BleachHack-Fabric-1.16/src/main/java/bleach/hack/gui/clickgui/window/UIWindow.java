/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.gui.clickgui.window;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.TriConsumer;

import bleach.hack.gui.clickgui.UIClickGuiScreen.Position;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

public class UIWindow extends ClickGuiWindow {

	private Supplier<int[]> sizeSupplier;
	private TriConsumer<MatrixStack, Integer, Integer> renderConsumer;

	private Map<String, MutablePair<Position, UIWindow>> otherWindows;

	public UIWindow(Map<String, MutablePair<Position, UIWindow>> otherWindows, Supplier<int[]> sizeSupplier, TriConsumer<MatrixStack, Integer, Integer> renderConsumer) {
		this(0, 0, otherWindows, sizeSupplier, renderConsumer);
	}

	public UIWindow(int x1, int y1, Map<String, MutablePair<Position, UIWindow>> otherWindows, Supplier<int[]> sizeSupplier, TriConsumer<MatrixStack, Integer, Integer> renderConsumer) {
		super(x1, y1, x1 + 80, y1 + 80, "", null);

		this.sizeSupplier = sizeSupplier;
		this.renderConsumer = renderConsumer;
		this.otherWindows = otherWindows;
	}

	public int[] getSize() {
		int[] size = sizeSupplier.get();
		return new int[] { size[0] + 2, size[1] + 2 };
	}

	public void renderUI(MatrixStack matrix) {
		renderConsumer.accept(matrix, x1 + 1, y1 + 1);
	}

	public void render(MatrixStack matrix, int mouseX, int mouseY) {
		// Snapping
		int sens = 5;
		if (dragging) {
			Entry<String, MutablePair<Position, UIWindow>> thisEntry = otherWindows.entrySet().stream()
					.filter(p -> p.getValue().right == this)
					.findFirst().orElse(null);

			// Really messy way to detach this from all its neighbors
			thisEntry.getValue().left.getAttachments().stream()
			.filter(p -> p.getLeft().length() > 1)
			.forEach(p -> otherWindows.get(p.getLeft()).left.getAttachments()
					.removeIf(a -> a.getLeft().equals(thisEntry.getKey())));

			x2 = (x2 - x1) + mouseX - dragOffX - Math.min(0, mouseX - dragOffX);
			y2 = (y2 - y1) + mouseY - dragOffY - Math.min(0, mouseY - dragOffY);
			x1 = Math.max(0, mouseX - dragOffX);
			y1 = Math.max(0, mouseY - dragOffY);

			Position newPos = new Position((double) x1 / mc.getWindow().getScaledWidth(), (double) y1 / mc.getWindow().getScaledHeight());

			if (mouseX - dragOffX < sens) {
				newPos.addAttachment(Pair.of("l", 1));
				x2 = x2 - x1;
				x1 = 0;
			} else if (mouseX - dragOffX + (x2 - x1) > mc.getWindow().getScaledWidth() - sens) {
				newPos.addAttachment(Pair.of("r", 3));
				x1 = mc.getWindow().getScaledWidth() - (x2 - x1);
				x2 = mc.getWindow().getScaledWidth();
			} else {
				for (Entry<String, MutablePair<Position, UIWindow>> e: otherWindows.entrySet()) {
					UIWindow window = e.getValue().right;
					if (window != this
							&& ((y1 >= window.y1 && y1 <= window.y2)
									|| (y2 >= window.y1 && y2 <= window.y2)
									|| (y1 <= window.y1 && y2 >= window.y2))) {
						if (Math.abs(window.x1 - x2) < sens) {
							newPos.addAttachment(Pair.of(e.getKey(), 3));
							x1 = window.x1 - (x2 - x1);
							x2 = window.x1;
						} else if (Math.abs(window.x2 - x1) < sens) {
							newPos.addAttachment(Pair.of(e.getKey(), 1));
							x2 = window.x2 + (x2 - x1);
							x1 = window.x2;
						}
					}
				}
			}

			if (mouseY - dragOffY < sens) {
				newPos.addAttachment(Pair.of("t", 2));
				y2 = y2 - y1;
				y1 = 0;
			} else if (mouseY - dragOffY + (y2 - y1) > mc.getWindow().getScaledHeight() - sens) {
				newPos.addAttachment(Pair.of("b", 0));
				y1 = mc.getWindow().getScaledHeight() - (y2 - y1);
				y2 = mc.getWindow().getScaledHeight();
			} else {
				for (Entry<String, MutablePair<Position, UIWindow>> e: otherWindows.entrySet()) {
					UIWindow window = e.getValue().right;
					if (window != this
							&& ((x1 >= window.x1 && x1 <= window.x2)
									|| (x2 >= window.x1 && x2 <= window.x2)
									|| (x1 <= window.x1 && x2 >= window.x2))) {
						if (Math.abs(window.y1 - y2) < sens) {
							newPos.addAttachment(Pair.of(e.getKey(), 0));
							y1 = window.y1 - (y2 - y1);
							y2 = window.y1;
						} else if (Math.abs(window.y2 - y1) < sens) {
							newPos.addAttachment(Pair.of(e.getKey(), 2));
							y2 = window.y2 + (y2 - y1);
							y1 = window.y2;
						}
					}
				}
			}


			thisEntry.getValue().left = newPos;
		}

		boolean realDragging = dragging;
		dragging = false;
		super.render(matrix, mouseX, mouseY);
		dragging = realDragging;

		renderUI(matrix);
	}

	protected void drawBar(MatrixStack matrix, int mouseX, int mouseY, TextRenderer textRend) {
		/* background */
		DrawableHelper.fill(matrix, x1, y1 + 1, x1 + 1, y2 - 1, 0xff6060b0);
		horizontalGradient(matrix, x1 + 1, y1, x2 - 1, y1 + 1, 0xff6060b0, 0xff8070b0);
		DrawableHelper.fill(matrix, x2 - 1, y1 + 1, x2, y2 - 1, 0xff8070b0);
		horizontalGradient(matrix, x1 + 1, y2 - 1, x2 - 1, y2, 0xff6060b0, 0xff8070b0);

		DrawableHelper.fill(matrix, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0x90606090);
	}

	public void mouseClicked(double mouseX, double mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);

		if (mouseX >= x1 && mouseX <= x2 - 2 && mouseY >= y1 && mouseY <= y2) {
			dragging = true;
			dragOffX = (int) mouseX - x1;
			dragOffY = (int) mouseY - y1;
		}
	}
}
