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
package bleach.hack.setting.base;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import bleach.hack.gui.clickgui.window.ModuleWindow;
import bleach.hack.gui.window.Window;
import bleach.hack.util.file.BleachFileHelper;
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

	public void setValue(double value) {
		this.value = value;
		BleachFileHelper.SCHEDULE_SAVE_MODULES = true;
	}

	public double round(double value, int places) {
		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public String getName() {
		return text;
	}

	public void render(ModuleWindow window, MatrixStack matrix, int x, int y, int len) {
		boolean mo = window.mouseOver(x, y, x + len, y + 12);
		if (mo) {
			DrawableHelper.fill(matrix, x + 1, y, x + len, y + 12, 0x70303070);
		}
		
		int pixels = (int) Math.round(MathHelper.clamp(len * ((getValue() - min) / (max - min)), 0, len));
		Window.horizontalGradient(matrix, x + 1, y, x + pixels, y + 12,
				mo ? 0xf03078b0 : 0xf03080a0, mo ? 0xf02068c0 : 0xf02070b0);

		MinecraftClient.getInstance().textRenderer.drawWithShadow(matrix,
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
