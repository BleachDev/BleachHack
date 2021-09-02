/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.gui.clickgui.window;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.util.TriConsumer;

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

	private boolean detachedFromOthers = false;

	public UIWindow(Position pos, UIContainer parentContainer, BooleanSupplier enabledSupplier, Supplier<int[]> sizeSupplier, TriConsumer<MatrixStack, Integer, Integer> renderConsumer) {
		super(0, 0, 0, 0, "", null);

		this.position = pos;
		this.parentContainer = parentContainer;
		this.enabledSupplier = enabledSupplier;
		this.sizeSupplier = sizeSupplier;
		this.renderConsumer = renderConsumer;
	}

	public int[] getSize() {
		int[] size = sizeSupplier.get();
		return new int[] { size[0] + 2, size[1] + 2 };
	}

	public void renderUI(MatrixStack matrices) {
		renderConsumer.accept(matrices, x1 + 1, y1 + 1);
	}

	public boolean shouldClose() {
		return !enabledSupplier.getAsBoolean();
	}

	private void detachFromOthers(boolean detachFromConstants) {
		// Really messy way to detach this from all its neighbors
		String thisId = parentContainer.windows.entrySet().stream()
				.filter(p -> p.getValue() == this)
				.map(e -> e.getKey())
				.findFirst().orElse(null);
		
		if (thisId == null)
			return;

		position.getAttachments().stream()
		.filter(p -> p.getLeft().length() > 1)
		.forEach(p -> parentContainer.windows.get(p.getLeft()).position.getAttachments()
				.removeIf(a -> a.getLeft().equals(thisId)));
		position.getAttachments().removeIf(p -> detachFromConstants || p.getLeft().length() > 1);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY) {
		if (shouldClose()) {
			if (!detachedFromOthers) {
				detachFromOthers(false);
				detachedFromOthers = true;
			}

			return;
		}

		detachedFromOthers = false;

		// Handling of attaching/detaching when dragging
		int sens = 5;
		if (dragging) {
			detachFromOthers(true);

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
				newPos.addAttachment(Pair.of("l", 1));
				x2 = wWidth - 1;
				x1 = -1;
			} else if (mouseX - dragOffX + wWidth > width - sens) {
				newPos.addAttachment(Pair.of("r", 3));
				x1 = width + 1 - wWidth;
				x2 = width + 1;
			} else if (Math.abs(mouseX - dragOffX + wWidth / 2 - width / 2) < sens) {
				newPos.addAttachment(Pair.of("c", 1));
				int w = wWidth;
				x1 = width / 2 - w / 2;
				x2 = x1 + w;
			} else {
				for (Entry<String, UIWindow> e: parentContainer.windows.entrySet()) {
					UIWindow window = e.getValue();
					if (window != this
							&& !window.shouldClose()
							&& ((y1 >= window.y1 && y1 <= window.y2)
									|| (y2 >= window.y1 && y2 <= window.y2)
									|| (y1 <= window.y1 && y2 >= window.y2))) {
						if (Math.abs(window.x1 - x2) < sens) {
							newPos.addAttachment(Pair.of(e.getKey(), 3));
							x1 = window.x1 - wWidth;
							x2 = window.x1;
						} else if (Math.abs(window.x2 - x1) < sens) {
							newPos.addAttachment(Pair.of(e.getKey(), 1));
							x2 = window.x2 + wWidth;
							x1 = window.x2;
						}
					}
				}
			}

			if (mouseY - dragOffY < sens) {
				newPos.addAttachment(Pair.of("t", 2));
				y2 = wHeight - 1;
				y1 = -1;
			} else if (mouseY - dragOffY + wHeight > parentContainer.getScreenBottom(height) - sens) {
				newPos.addAttachment(Pair.of("b", 0));
				y1 = height + 1 - wHeight;
				y2 = height + 1;
			} else {
				for (Entry<String, UIWindow> e: parentContainer.windows.entrySet()) {
					UIWindow window = e.getValue();
					if (window != this
							&& !window.shouldClose()
							&& ((x1 >= window.x1 && x1 <= window.x2)
									|| (x2 >= window.x1 && x2 <= window.x2)
									|| (x1 <= window.x1 && x2 >= window.x2))) {
						if (Math.abs(window.y1 - y2) < sens) {
							newPos.addAttachment(Pair.of(e.getKey(), 0));
							y1 = window.y1 - wHeight;
							y2 = window.y1;
						} else if (Math.abs(window.y2 - y1) < sens) {
							newPos.addAttachment(Pair.of(e.getKey(), 2));
							y2 = window.y2 + wHeight;
							y1 = window.y2;
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

	protected void drawBar(MatrixStack matrices, int mouseX, int mouseY, TextRenderer textRend) {
		/* background */
		DrawableHelper.fill(matrices, x1, y1 + 1, x1 + 1, y2 - 1, 0xff6060b0);
		horizontalGradient(matrices, x1 + 1, y1, x2 - 1, y1 + 1, 0xff6060b0, 0xff8070b0);
		DrawableHelper.fill(matrices, x2 - 1, y1 + 1, x2, y2 - 1, 0xff8070b0);
		horizontalGradient(matrices, x1 + 1, y2 - 1, x2 - 1, y2, 0xff6060b0, 0xff8070b0);

		DrawableHelper.fill(matrices, x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0x90606090);
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
		private List<Pair<String, Integer>> attachments = new ArrayList<>();

		public Position(double xPercent, double yPercent) {
			this.xPercent = xPercent;
			this.yPercent = yPercent;
		}

		public Position(double xPercent, double yPercent, Pair<String, Integer> attachment) {
			this.xPercent = xPercent;
			this.yPercent = yPercent;
			addAttachment(attachment);
		}

		public Position(Pair<String, Integer> attachment1, Pair<String, Integer> attachment2) {
			addAttachment(attachment1);
			addAttachment(attachment2);
		}

		public List<Pair<String, Integer>> getAttachments() {
			return attachments;
		}

		public boolean addAttachment(Pair<String, Integer> attachment) {
			if (attachments.isEmpty()) {
				attachments.add(attachment);
				return true;
			}

			if (attachments.size() == 1) {
				int side = attachments.get(0).getRight();
				int newSide = attachment.getRight();
				if (newSide != side && newSide != side + 2 % 4) {
					attachments.add(attachment);
					return true;
				}

			}

			return false;
		}
	}
}
