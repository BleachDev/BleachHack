/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.setting.base;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.bleachhack.gui.clickgui.window.ModuleWindow;
import org.bleachhack.gui.window.Window;
import org.bleachhack.util.io.BleachFileHelper;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class SettingSlider extends SettingBase {

	public double min;
	public double max;
	private double value;
	public int decimals;
	public String text;

	protected double defaultValue;

	public SettingSlider(String text, double min, double max, double value, int decimals) {
		this.min = min;
		this.max = max;
		this.value = value;
		this.decimals = decimals;
		this.text = text;

		defaultValue = value;
	}

	public double getValue() {
		return round(value, decimals);
	}
	
	public float getValueFloat() {
		return (float) getValue();
	}
	
	public int getValueInt() {
		return (int) getValue();
	}
	
	public long getValueLong() {
		return (long) getValue();
	}

	public void setValue(double value) {
		this.value = value;
		BleachFileHelper.SCHEDULE_SAVE_MODULES.set(true);
	}

	public double round(double value, int places) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public String getName() {
		return text;
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
				text + ": " + (decimals == 0 ? Integer.toString((int) getValue()) : getValue()),
				x + 3, y + 2, 0xcfe0cf);

		if (window.mouseOver(x + 1, y, x + len, y + 12)) {
			if (window.lmHeld) {
				int percent = ((window.mouseX - x) * 100) / len;

				setValue(round(percent * ((max - min) / 100) + min, decimals));
			}

			if (window.mwScroll != 0) {
				double units = 1 / (Math.pow(10, decimals));

				setValue(MathHelper.clamp(getValue() + units * window.mwScroll, min, max));
				MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
			}
		}
	}

	public SettingSlider withDesc(String desc) {
		description = desc;
		return this;
	}

	public int getHeight(int len) {
		return 12;
	}

	public void readSettings(JsonElement settings) {
		if (settings.isJsonPrimitive()) {
			setValue(settings.getAsDouble());
		}
	}

	public JsonElement saveSettings() {
		return new JsonPrimitive(getValue());
	}

	@Override
	public boolean isDefault() {
		BigDecimal bd = new BigDecimal(defaultValue);
		bd = bd.setScale(decimals, RoundingMode.HALF_UP);

		return bd.doubleValue() == getValue();
	}
}
