/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.setting.module;

import org.bleachhack.gui.clickgui.window.ModuleWindow;
import org.bleachhack.setting.SettingDataHandlers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

public class SettingButton extends ModuleSetting<Void> {

	public Runnable action;

	public SettingButton(String text, Runnable action) {
		super(text, null, SettingDataHandlers.NULL);
		this.action = action;
	}

	public void render(ModuleWindow window, MatrixStack matrices, int x, int y, int len) {
		if (window.mouseOver(x, y, x + len, y + 12)) {
			DrawableHelper.fill(matrices, x + 1, y, x + len, y + 12, 0x70303070);
		}

		MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, getName(), x + 3, y + 2, 0xcfe0cf);

		if (window.mouseOver(x, y, x + len, y + 12) && window.lmDown) {
			window.mouseReleased(window.mouseX, window.mouseY, 1);
			MinecraftClient.getInstance().currentScreen.mouseReleased(window.mouseX, window.mouseY, 0);
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
			action.run();
		}
	}

	public SettingButton withDesc(String desc) {
		setTooltip(desc);
		return this;
	}

	public int getHeight(int len) {
		return 12;
	}
}
