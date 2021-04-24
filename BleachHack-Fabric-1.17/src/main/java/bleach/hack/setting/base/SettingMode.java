/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.setting.base;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import bleach.hack.gui.clickgui.window.ModuleWindow;
import bleach.hack.util.file.BleachFileHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class SettingMode extends SettingBase {

	public String[] modes;
	public int mode;
	public String text;

	public SettingMode(String text, String... modes) {
		this.modes = modes;
		this.text = text;
	}

	public int getNextMode() {
		if (mode + 1 >= modes.length) {
			return 0;
		}

		return mode + 1;
	}

	public String getName() {
		return text;
	}

	public void render(ModuleWindow window, MatrixStack matrix, int x, int y, int len) {
		if (window.mouseOver(x, y, x + len, y + 12)) {
			DrawableHelper.fill(matrix, x + 1, y, x + len, y + 12, 0x70303070);
		}
		
		MinecraftClient.getInstance().textRenderer.drawWithShadow(matrix, text + ": " + modes[mode], x + 3, y + 2, 0xcfe0cf);

		if (window.mouseOver(x, y, x + len, y + 12) && window.lmDown) {
			mode = getNextMode();
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
			BleachFileHelper.SCHEDULE_SAVE_MODULES = true;
		}
	}

	public SettingMode withDesc(String desc) {
		description = desc;
		return this;
	}

	public int getHeight(int len) {
		return 12;
	}

	public void readSettings(JsonElement settings) {
		if (settings.isJsonPrimitive()) {
			mode = MathHelper.clamp(settings.getAsInt(), 0, modes.length);
		}
	}

	public JsonElement saveSettings() {
		return new JsonPrimitive(MathHelper.clamp(mode, 0, modes.length));
	}

	@Override
	public boolean isDefault() {
		return mode == 0;
	}
}
