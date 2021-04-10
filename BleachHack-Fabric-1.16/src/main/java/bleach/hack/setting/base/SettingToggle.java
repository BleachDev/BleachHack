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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Triple;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import bleach.hack.gui.clickgui.window.ModuleWindow;
import bleach.hack.util.file.BleachFileHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

public class SettingToggle extends SettingBase {

	public boolean state;
	public String text;

	protected boolean defaultState;

	protected List<SettingBase> children = new ArrayList<>();
	protected boolean expanded = false;

	public SettingToggle(String text, boolean state) {
		this.state = state;
		this.text = text;

		defaultState = state;
	}

	public String getName() {
		return text;
	}

	public void render(ModuleWindow window, MatrixStack matrix, int x, int y, int len) {
		String color2 = state ? "\u00a7a" : "\u00a7c";

		if (window.mouseOver(x, y, x + len, y + 12)) {
			DrawableHelper.fill(matrix, x + 1, y, x + len, y + 12, 0x70303070);
		}

		if (!children.isEmpty()) {
			if (window.rmDown && window.mouseOver(x, y, x + len, y + 12)) {
				expanded = !expanded;
				MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
			}

			if (expanded) {
				DrawableHelper.fill(matrix, x + 2, y + 12, x + 3, y + getHeight(len) - 1, 0xff8070b0);

				int h = y + 12;
				for (SettingBase s : children) {
					s.render(window, matrix, x + 2, h, len - 2);

					h += s.getHeight(len - 3);
				}
			}

			if (expanded) {
				MinecraftClient.getInstance().textRenderer.drawWithShadow(matrix,
						color2 + "\u2228",
						x + len - 8, y + 3, -1);
			} else {
				matrix.push();

				matrix.scale(0.75f, 0.75f, 1f);
				MinecraftClient.getInstance().textRenderer.drawWithShadow(matrix,
						color2 + "\u00a7l>",
						(int) ((x + len - 7) * 1 / 0.75), (int) ((y + 4) * 1 / 0.75), -1);

				matrix.pop();
			}
		}

		MinecraftClient.getInstance().textRenderer.drawWithShadow(matrix, color2 + text, x + 3, y + 2, 0xffffff);

		if (window.mouseOver(x, y, x + len, y + 12) && window.lmDown) {
			state = !state;
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
			BleachFileHelper.SCHEDULE_SAVE_MODULES = true;
		}
	}

	public int getHeight(int len) {
		int h = 12;

		if (expanded) {
			h += 1;
			for (SettingBase s : children)
				h += s.getHeight(len - 2);
		}

		return h;
	}

	public SettingBase getChild(int c) {
		return children.get(c);
	}

	public SettingToggle withChildren(SettingBase... children) {
		this.children.addAll(Arrays.asList(children));
		return this;
	}

	public SettingToggle withDesc(String desc) {
		description = desc;
		return this;
	}

	public Triple<Integer, Integer, String> getGuiDesc(ModuleWindow window, int x, int y, int len) {
		if (!expanded || window.mouseY - y <= 12)
			return super.getGuiDesc(window, x, y, len);

		Triple<Integer, Integer, String> triple = null;

		int h = y + 12;
		for (SettingBase s : children) {
			if (window.mouseOver(x + 2, h, x + len, h + s.getHeight(len))) {
				triple = s.getGuiDesc(window, x + 2, h, len - 2);
			}

			h += s.getHeight(len - 2);
		}

		return triple;
	}

	public void readSettings(JsonElement settings) {
		if (settings.isJsonPrimitive()) {
			state = settings.getAsBoolean();
		} else if (settings.isJsonObject()) {
			JsonObject jo = settings.getAsJsonObject();
			if (!jo.has("toggled"))
				return;

			state = jo.get("toggled").getAsBoolean();

			for (Entry<String, JsonElement> e : jo.get("children").getAsJsonObject().entrySet()) {
				for (SettingBase s : children) {
					if (s.getName().equals(e.getKey())) {
						s.readSettings(e.getValue());
					}
				}
			}
		}
	}

	public JsonElement saveSettings() {
		if (children.isEmpty()) {
			return new JsonPrimitive(state);
		} else {
			JsonObject jo = new JsonObject();
			jo.add("toggled", new JsonPrimitive(state));

			JsonObject subJo = new JsonObject();
			for (SettingBase s : children) {
				subJo.add(s.getName(), s.saveSettings());
			}

			jo.add("children", subJo);
			return jo;
		}
	}

	@Override
	public boolean isDefault() {
		if (state != defaultState)
			return false;

		for (SettingBase s : children) {
			if (!s.isDefault())
				return false;
		}

		return true;
	}
}
