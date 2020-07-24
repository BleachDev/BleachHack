/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
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
package bleach.hack.gui.clickgui;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import bleach.hack.gui.clickgui.modulewindow.ModuleWindow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

public class SettingToggle extends SettingBase {

	public boolean state;
	public String text;

	public SettingToggle(String text, boolean state) {
		this.state = state;
		this.text = text;
	}
	
	public String getName() {
		return text;
	}
	
	public void render(ModuleWindow window, MatrixStack matrix, int x, int y, int len, int mouseX, int mouseY, boolean lmDown, boolean rmDown, boolean lmHeld) {
		String color2;

		if (state) { 
			if (window.mouseOver(x, y, x+len, y+12)) color2 = "\u00a72";
			else color2 = "\u00a7a";
		} else {
			if (window.mouseOver(x, y, x+len, y+12)) color2 = "\u00a74";
			else color2 = "\u00a7c";
		}

		window.fillGreySides(matrix, x, y-1, x+len-1, y+12);
		MinecraftClient.getInstance().textRenderer.drawWithShadow(matrix, color2 + text, x + 3, y + 2, 0xffffff);

		if (window.mouseOver(x, y, x+len, y+12) && lmDown) state = !state;
	}
	
	public int getHeight(int len) {
		return 12;
	}
	
	public void readSettings(JsonElement settings) {
		if (settings.isJsonPrimitive()) {
			state = settings.getAsBoolean();
		}
	}

	public JsonElement saveSettings() {
		return new JsonPrimitive(state);
	}
}
