/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.gui.window;

import java.util.List;
import org.apache.commons.lang3.tuple.Triple;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;

public class WindowManagerScreen extends Screen {

	/** [Window Screen, Name, Icon] **/
	public Triple<WindowScreen, String, ItemStack>[] windows;
	private int selected;

	@SafeVarargs
	public WindowManagerScreen(Triple<WindowScreen, String, ItemStack>... windows) {
		super(LiteralText.EMPTY);
		this.windows = windows;
	}

	public void init() {
		selectWindow(selected);
	}

	@SuppressWarnings("unchecked")
	public void selectWindow(int s) {
		selected = s;
		for (Triple<WindowScreen, String, ItemStack> t: windows) {
			remove(t.getLeft());
		}

		getSelectedScreen().init(client, width, height - 14);
		addDrawable(getSelectedScreen());
		((List<Element>) children()).add(getSelectedScreen());
	}

	public WindowScreen getSelectedScreen() {
		return windows[selected].getLeft();
	}

	public String getSelectedTitle() {
		return windows[selected].getMiddle();
	}

	public ItemStack getSelectedIcon() {
		return windows[selected].getRight();
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		DrawableHelper.fill(matrices, 0, height - 13, 19, height - 12, 0xff6060b0);
		DrawableHelper.fill(matrices, 0, height - 13, 1, height, 0xff6060b0);
		DrawableHelper.fill(matrices, 19, height - 12, 20, height, 0xff6060b0);
		textRenderer.draw(matrices, "\u00a7cX", 7, height - 10, -1);

		int wid = 20;
		int size = Math.min((width - wid) / windows.length, 90);
		for (int i = 0; i < windows.length; i++) {
			DrawableHelper.fill(matrices, wid, height - 13, wid + size - 1, height - 12, 0xff6060b0);
			DrawableHelper.fill(matrices, wid, height - 13, wid + 1, height, 0xff6060b0);
			DrawableHelper.fill(matrices, wid + size - 1, height - 12, wid + size, height, 0xff6060b0);

			RenderSystem.getModelViewStack().push();
			RenderSystem.getModelViewStack().scale(0.7f, 0.7f, 1f);

			itemRenderer.renderGuiItemIcon(windows[i].getRight(), (int) ((wid + 2) * (1 / 0.7)), (int) ((height - 12) * (1 / 0.7)));

			RenderSystem.getModelViewStack().pop();
			RenderSystem.applyModelViewMatrix();

			textRenderer.draw(matrices, windows[i].getMiddle(), wid + 16, height - 10, selected == i ? 0xffccff : 0xffffff);
			wid += size;
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (mouseX > 0 && mouseX < 20 && mouseY > height - 14 && mouseY < height) {
			selectWindow(0);

			MinecraftClient.getInstance().getSoundManager().play(
					PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
		}

		if (mouseY > height - 14 && mouseY < height && mouseX > 20) {
			int sel = ((int) mouseX - 20) / Math.min((width - 20) / windows.length, 90);

			if (sel >= 0 && sel < windows.length) {
				selectWindow(sel);

				MinecraftClient.getInstance().getSoundManager().play(
						PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	// Children don't tick brue
	@Override
	public void tick() {
		getSelectedScreen().tick();
		super.tick();
	}

	// Children also don't take keyboard input brueh
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		getSelectedScreen().keyPressed(keyCode, scanCode, modifiers);
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		getSelectedScreen().keyReleased(keyCode, scanCode, modifiers);
		return super.keyReleased(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		getSelectedScreen().charTyped(chr, modifiers);
		return super.charTyped(chr, modifiers);
	}
}
