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

import com.google.gson.JsonElement;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class SettingMode extends ModuleSetting<Integer> {

	public String[] modes;

	public SettingMode(String text, String... modes) {
		super(text, 0, SettingDataHandlers.INTEGER);
		this.modes = modes;
	}
	
	public int getMode() {
		return getValue().intValue();
	}

	public void render(ModuleWindow window, MatrixStack matrices, int x, int y, int len) {
		if (window.mouseOver(x, y, x + len, y + 12)) {
			DrawableHelper.fill(matrices, x + 1, y, x + len, y + 12, 0x70303070);
		}

		MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, getName() + ": " + modes[getValue()], x + 3, y + 2, 0xcfe0cf);

		if (window.mouseOver(x, y, x + len, y + 12) && window.lmDown) {
			setValue(getValue() >= modes.length - 1 ? 0 : getValue() + 1);
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
		}
	}

	public SettingMode withDesc(String desc) {
		setTooltip(desc);
		return this;
	}

	public int getHeight(int len) {
		return 12;
	}

	@Override
	public void read(JsonElement json) {
		Integer val = getHandler().readOrNull(json);
		if (val != null)
			setValue(MathHelper.clamp(val, 0, modes.length - 1));
	}
}
