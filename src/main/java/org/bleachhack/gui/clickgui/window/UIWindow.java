/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.gui.clickgui.window;

import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.apache.logging.log4j.util.TriConsumer;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

// this is worse than dispenser32k
// gonna have no idea what any of this does in 2 weeks
public class UIWindow extends ClickGuiWindow {

	public Position position;

	private BooleanSupplier enabledSupplier;
	private Supplier<int[]> sizeSupplier;
	private TriConsumer<MatrixStack, Integer, Integer> renderConsumer;

	private UIContainer parentContainer;

	public UIWindow(Position pos, UIContainer parentContainer, BooleanSupplier enabledSupplier, Supplier<int[]> sizeSupplier, TriConsumer<MatrixStack, Integer, Integer> renderConsumer) {
		super(0, 0, 0, 0, "", null);

		this.position = pos;
		this.parentContainer = parentContainer;
		this.enabledSupplier = enabledSupplier;
		this.sizeSupplier = sizeSupplier;
		this.renderConsumer = renderConsumer;
	}

	public int[] getSize() {
		return sizeSupplier.get();
	}

	public void renderUI(MatrixStack matrices) {
		renderConsumer.accept(matrices, x1, y1);
	}

	public boolean shouldClose() {
		return !enabledSupplier.getAsBoolean();
	}

	public void detachFromOthers(boolean detachFromConstants) {
		String thisId = parentContainer.getIdFromWindow(this);

		if (thisId == null)
			return;

		position.getAttachments().keySet().stream()
		.filter(id -> id.length() > 1)
		.forEach(id -> parentContainer.windows.get(id).position.getAttachments().keySet()
				.removeIf(id1 -> id1.equals(thisId)));
		position.getAttachments().keySet().removeIf(id -> detachFromConstants || id.length() > 1);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY) {
		// Handling of attaching/detaching when dragging
		int sens = 5;
		if (dragging) {
			detachFromOthers(true);

			String thisId = parentContainer.getIdFromWindow(this);
			int width = mc.getWindow().getScaledWidth();
			int height = mc.getWindow().getScaledHeight();
			int wWidth = x2 - x1;
			int wHeight = y2 - y1;

			x2 = wWidth + mouseX - dragOffX - Math.min(0, mouseX - dragOffX);
			y2 = wHeight + mouseY - dragOffY - Math.min(0, mouseY - dragOffY);
			x1 = Math.max(0, mouseX - dragOffX);
			y1 = Math.max(0, mouseY - dragOffY);

			Position newPos = new Position((double) x1 / width, (double) y1 / height);

			if (mouseX - dragOffX < sens) {
				newPos.addAttachment("l", 1);
				x2 = wWidth;
				x1 = 0;
			} else if (mouseX - dragOffX + wWidth > width - sens) {
				newPos.addAttachment("r", 3);
				x1 = width - wWidth;
				x2 = width;
			} else if (Math.abs(mouseX - dragOffX + wWidth / 2 - width / 2) < sens) {
				newPos.addAttachment("c", 1);
				x1 = width / 2 - wWidth / 2;
				x2 = x1 + wWidth;
			} else {
				for (Entry<String, UIWindow> e: parentContainer.windows.entrySet()) {
					UIWindow window = e.getValue();
					if (window != this
							&& !window.shouldClose()
							&& ((y1 >= window.y1 && y1 <= window.y2)
									|| (y2 >= window.y1 && y2 <= window.y2)
									|| (y1 <= window.y1 && y2 >= window.y2))) {
						if (Math.abs(window.x1 - x2) < sens) {
							if (newPos.addAttachment(e.getKey(), 3)) {
								x1 = window.x1 - wWidth;
								x2 = window.x1;
							} else if (window.position.addAttachment(thisId, 1)) {
								window.x2 = x2 + (window.x2 - window.x1);
								window.x1 = x2;
							}
						} else if (Math.abs(window.x2 - x1) < sens) {
							if (newPos.addAttachment(e.getKey(), 1)) {
								x2 = window.x2 + wWidth;
								x1 = window.x2;
							} else if (window.position.addAttachment(thisId, 3)) {
								window.x1 = x1 - (window.x2 - window.x1);
								window.x2 = x1;
							}
						}
					}
				}
			}

			if (mouseY - dragOffY < sens) {
				newPos.addAttachment("t", 2);
				y2 = wHeight;
				y1 = 0;
			} else if (mouseY - dragOffY + wHeight > parentContainer.getScreenBottom(height) - sens) {
				newPos.addAttachment("b", 0);
				y1 = height - wHeight;
				y2 = height;
			} else {
				for (Entry<String, UIWindow> e: parentContainer.windows.entrySet()) {
					UIWindow window = e.getValue();
					if (window != this
							&& !window.shouldClose()
							&& ((x1 >= window.x1 && x1 <= window.x2)
									|| (x2 >= window.x1 && x2 <= window.x2)
									|| (x1 <= window.x1 && x2 >= window.x2))) {
						if (Math.abs(window.y1 - y2) < sens) {
							if (newPos.addAttachment(e.getKey(), 0)) {
								y1 = window.y1 - wHeight;
								y2 = window.y1;
							} else if (window.position.addAttachment(thisId, 2)) {
								window.y2 = y2 + (window.y2 - window.y1);
								window.y1 = y2;
							}
						} else if (Math.abs(window.y2 - y1) < sens) {
							if (newPos.addAttachment(e.getKey(), 2)) {
								y2 = window.y2 + wHeight;
								y1 = window.y2;
							} else if (window.position.addAttachment(thisId, 0)) {
								window.y1 = y1 - (window.y2 - window.y1);
								window.y2 = y1;
							}
						}
					}
				}
			}


			position = newPos;
		}

		boolean realDragging = dragging;
		dragging = false;
		super.render(matrices, mouseX, mouseY);
		dragging = realDragging;

		renderUI(matrices);
	}

	protected void drawBackground(MatrixStack matrices, int mouseX, int mouseY, TextRenderer textRend) {
		// background
		/*DrawableHelper.fill(matrices, x1, y1 + 1, x1 + 1, y2 - 1, 0xff6060b0);
		horizontalGradient(matrices, x1 + 1, y1, x2 - 1, y1 + 1, 0xff6060b0, 0xff8070b0);
		DrawableHelper.fill(matrices, x2 - 1, y1 + 1, x2, y2 - 1, 0xff8070b0);
		horizontalGradient(matrices, x1 + 1, y2 - 1, x2 - 1, y2, 0xff6060b0,  0xff8070b0);*/
		
		// limited debug edition
		/*DrawableHelper.fill(matrices, x1, y1 + 1, x1 + 1, y2 - 1,
				position.getAttachments().containsValue(1) ? 0xff60b060 : 0xff6060b0);
		horizontalGradient(matrices, x1 + 1, y1, x2 - 1, y1 + 1,
				(position.getAttachments().containsValue(2) ? 0xff60b060 : 0xff6060b0),
				(position.getAttachments().containsValue(2) ? 0xff80c060 : 0xff8070b0));
		DrawableHelper.fill(matrices, x2 - 1, y1 + 1, x2, y2 - 1,
				position.getAttachments().containsValue(3) ? 0xff80c060 : 0xff8070b0);
		horizontalGradient(matrices, x1 + 1, y2 - 1, x2 - 1, y2,
				(position.getAttachments().containsValue(0) ? 0xff60b060 : 0xff6060b0),
				(position.getAttachments().containsValue(0) ? 0xff80c060 : 0xff8070b0));*/

		DrawableHelper.fill(matrices, x1, y1, x2, y2, 0x90606090);
	}

	public void mouseClicked(double mouseX, double mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);

		if (mouseX >= x1 && mouseX <= x2 - 2 && mouseY >= y1 && mouseY <= y2) {
			dragging = true;
			dragOffX = (int) mouseX - x1;
			dragOffY = (int) mouseY - y1;
		}
	}

	public static class Position {

		public double xPercent;
		public double yPercent;
		private Object2IntMap<String> attachments = new Object2IntOpenHashMap<>(2);

		public Position(double xPercent, double yPercent) {
			this.xPercent = xPercent;
			this.yPercent = yPercent;
		}

		public Position(double xPercent, double yPercent, String atmName, int atmSide) {
			this.xPercent = xPercent;
			this.yPercent = yPercent;
			addAttachment(atmName, atmSide);
		}

		public Position(String atmName1, int atmSide1, String atmName2, int atmSide2) {
			addAttachment(atmName1, atmSide1);
			addAttachment(atmName2, atmSide2);
		}

		public Object2IntMap<String> getAttachments() {
			return attachments;
		}

		public boolean addAttachment(String atmName, int atmSide) {
			if (attachments.isEmpty()) {
				attachments.put(atmName, atmSide);
				return true;
			}

			if (attachments.size() == 1) {
				int side = attachments.values().iterator().nextInt();
				if (atmSide != side && atmSide != (side + 2) % 4) {
					attachments.put(atmName, atmSide);
					return true;
				}

			}

			return false;
		}
	}
}
