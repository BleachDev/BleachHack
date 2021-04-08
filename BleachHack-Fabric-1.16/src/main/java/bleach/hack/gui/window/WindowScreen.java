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
package bleach.hack.gui.window;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public abstract class WindowScreen extends Screen {

	private List<Window> windows = new ArrayList<>();

	/* [Layer, Window Index] */
	private SortedMap<Integer, Integer> windowOrder = new TreeMap<>(); 

	public WindowScreen(Text title) {
		super(title);
	}

	public void addWindow(Window window) {
		windows.add(window);
		windowOrder.put(windows.size() - 1, windows.size() - 1);
	}

	public Window getWindow(int i) {
		return windows.get(i);
	}

	public void clearWindows() {
		windows.clear();
		windowOrder.clear();
	}

	public List<Window> getWindows() {
		return windows;
	}

	protected List<Integer> getWindowsBackToFront() {
		return windowOrder.values().stream().collect(Collectors.toList());
	}

	protected List<Integer> getWindowsFrontToBack() {
		List<Integer> w = getWindowsBackToFront();
		Collections.reverse(w);
		return w;
	}

	protected int getSelectedWindow() {
		for (int i = 0; i < windows.size(); i++) {
			if (!getWindow(i).closed && getWindow(i).selected) {
				return i;
			}
		}

		return -1;
	}

	public void init() {
		super.init();
	}

	public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
		int sel = getSelectedWindow();

		if (sel == -1) {
			for (int i: getWindowsFrontToBack()) {
				if (!getWindow(i).closed) {
					selectWindow(i);
					break;
				}
			}
		}

		boolean close = true;

		for (int w: getWindowsBackToFront()) {
			if (!getWindow(w).closed) {
				close = false;
				onRenderWindow(matrix, w, mouseX, mouseY);
			}
		}

		if (close) this.onClose();

		super.render(matrix, mouseX, mouseY, delta);
	}

	public void onRenderWindow(MatrixStack matrix, int window, int mouseX, int mouseY) {
		if (!windows.get(window).closed) {
			windows.get(window).render(matrix, mouseX, mouseY);
		}
	}

	public void selectWindow(int window) {
		for (Window w: windows) {
			if (w.selected) {
				w.inactiveTime = 2;
			}

			w.selected = false;
		}

		for (int i = 0; i < windows.size(); i++) {
			Window w = windows.get(i);

			if (i == window) {
				w.selected = true;
				int index = -1;
				for (Entry<Integer, Integer> e: windowOrder.entrySet()) {
					if (e.getValue() == window) {
						index = e.getKey();
						break;
					}
				}

				windowOrder.remove(index);
				for (Entry<Integer, Integer> e: new TreeMap<>(windowOrder).entrySet()) {
					if (e.getKey() > index) {
						windowOrder.remove(e.getKey());
						windowOrder.put(e.getKey() - 1, e.getValue());
					}
				}

				windowOrder.put(windowOrder.size(), window);
			}
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		/* Handle what window will be selected when clicking */
		for (int wi: getWindowsFrontToBack()) {
			Window w = getWindow(wi);

			if (w.inactiveTime <= 0 && mouseX > w.x1 && mouseX < w.x2 && mouseY > w.y1 && mouseY < w.y2 && !w.closed) {
				if (w.shouldClose((int) mouseX, (int) mouseY)) {
					w.closed = true;
					break;
				}

				if (w.selected) {
					w.onMousePressed((int) mouseX, (int) mouseY);
				} else {
					selectWindow(wi);
				}

				break;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		for (Window w : windows) {
			w.onMouseReleased((int) mouseX, (int) mouseY);
		}

		return super.mouseReleased(mouseX, mouseY, button);
	}

	public void renderBackgroundTexture(int vOffset) {
		Color colorTL = new Color(100, 120, 0);
		Color colorTR = new Color(70, 120, 20);
		Color colorBL = new Color(60, 160, 0);
		Color colorBR = new Color(60, 200, 60);

		RenderSystem.enableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.shadeModel(GL11.GL_SMOOTH);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
		bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(width, 0, 0).color(colorTR.getRed(), colorTR.getBlue(), colorTR.getGreen(), 255).next();
		bufferBuilder.vertex(0, 0, 0).color(colorTL.getRed(), colorTL.getBlue(), colorTL.getGreen(), 255).next();
		bufferBuilder.vertex(0, height + 14, 0).color(colorBL.getRed(), colorBL.getBlue(), colorBL.getGreen(), 255).next();
		bufferBuilder.vertex(width, height + 14, 0).color(colorBR.getRed(), colorBR.getBlue(), colorBR.getGreen(), 255).next();
		tessellator.draw();

		RenderSystem.shadeModel(GL11.GL_FLAT);
		RenderSystem.disableBlend();
		RenderSystem.disableTexture();
	}
}
