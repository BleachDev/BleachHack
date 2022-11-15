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
import org.bleachhack.module.Module;
import org.bleachhack.setting.SettingDataHandlers;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

public class SettingKey extends ModuleSetting<Integer> {

	public SettingKey(int key) {
		super("Bind", key, SettingDataHandlers.INTEGER);
	}

	@Override
	public void render(ModuleWindow window, MatrixStack matrices, int x, int y, int len) {
		if (window.mouseOver(x, y, x + len, y + 12)) {
			DrawableHelper.fill(matrices, x + 1, y, x + len, y + 12, 0x70303070);
		}
		
		if (window.keyDown >= 0 && window.keyDown != GLFW.GLFW_KEY_ESCAPE && window.mouseOver(x, y, x + len, y + 12)) {
			setValue(window.keyDown == GLFW.GLFW_KEY_DELETE ? Module.KEY_UNBOUND : window.keyDown);
			MinecraftClient.getInstance().getSoundManager().play(
					PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
		}

		int key = getValue();
		String name = key < 0 ? "NONE" : InputUtil.fromKeyCode(key, -1).getLocalizedText().getString();
		if (name == null)
			name = "KEY" + key;
		else if (name.isEmpty())
			name = "NONE";

		MinecraftClient.getInstance().textRenderer.drawWithShadow(
				matrices, "Bind: " + name + (window.mouseOver(x, y, x + len, y + 12) ? "..." : ""), x + 3, y + 2, 0xcfe0cf);
	}

	public SettingKey withDesc(String desc) {
		setTooltip(desc);
		return this;
	}

	@Override
	public int getHeight(int len) {
		return 12;
	}
}
