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
import com.google.gson.JsonNull;
import bleach.hack.gui.clickgui.window.ModuleWindow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

public class SettingButton extends SettingBase {

	public String text;
	public Runnable action;

	public SettingButton(String text, Runnable action) {
		this.text = text;
		this.action = action;
	}

	public String getName() {
		return text;
	}

	public void render(ModuleWindow window, MatrixStack matrices, int x, int y, int len) {
		if (window.mouseOver(x, y, x + len, y + 12)) {
			DrawableHelper.fill(matrices, x + 1, y, x + len, y + 12, 0x70303070);
		}
		
		MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, text, x + 3, y + 2, 0xcfe0cf);

		if (window.mouseOver(x, y, x + len, y + 12) && window.lmDown) {
			window.mouseReleased(window.mouseX, window.mouseY, 1);
			MinecraftClient.getInstance().currentScreen.mouseReleased(window.mouseX, window.mouseY, 0);
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
			action.run();
		}
	}

	public SettingButton withDesc(String desc) {
		description = desc;
		return this;
	}

	public int getHeight(int len) {
		return 12;
	}

	public void readSettings(JsonElement settings) {
	}

	public JsonElement saveSettings() {
		return JsonNull.INSTANCE;
	}

	@Override
	public boolean isDefault() {
		return true;
	}
}
