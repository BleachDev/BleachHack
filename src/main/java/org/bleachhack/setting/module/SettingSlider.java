/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.setting.module;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.bleachhack.gui.clickgui.window.ModuleWindow;
import org.bleachhack.gui.window.Window;
import org.bleachhack.setting.SettingDataHandlers;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class SettingSlider extends ModuleSetting<Double> {

	public double min;
	public double max;
	public int decimals;

	public SettingSlider(String text, double min, double max, double value, int decimals) {
		super(text, value, SettingDataHandlers.DOUBLE);
		this.min = min;
		this.max = max;
		this.decimals = decimals;
	}

	public float getValueFloat() {
		return getValue().floatValue();
	}

	public int getValueInt() {
		return getValue().intValue();
	}

	public long getValueLong() {
		return getValue().longValue();
	}

	public void render(ModuleWindow window, MatrixStack matrices, int x, int y, int len) {
		boolean mo = window.mouseOver(x, y, x + len, y + 12);
		if (mo) {
			DrawableHelper.fill(matrices, x + 1, y, x + len, y + 12, 0x70303070);
		}

		int pixels = (int) Math.round(MathHelper.clamp(len * ((getValue() - min) / (max - min)), 0, len));
		Window.horizontalGradient(matrices, x + 1, y, x + pixels, y + 12,
				mo ? 0xf03078b0 : 0xf03080a0, mo ? 0xf02068c0 : 0xf02070b0);

		MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices,
				getName() + ": " + (decimals == 0 ? Integer.toString(getValueInt()) : getValue()),
				x + 3, y + 2, 0xcfe0cf);

		if (window.mouseOver(x + 1, y, x + len, y + 12)) {
			if (window.lmHeld) {
				int percent = (window.mouseX - x) * 100 / len;

				setValue(percent * ((max - min) / 100) + min);
			}

			if (window.mwScroll != 0) {
				double units = 1 / (Math.pow(10, decimals));

				setValue(MathHelper.clamp(getValue() + units * window.mwScroll, min, max));
				MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
			}
		}
	}

	public SettingSlider withDesc(String desc) {
		setTooltip(desc);
		return this;
	}

	public int getHeight(int len) {
		return 12;
	}

	@Override
	public void setValue(Double value) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(decimals, RoundingMode.HALF_UP);

		super.setValue(bd.doubleValue());
	}
}
