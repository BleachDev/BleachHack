/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.setting.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.bleachhack.gui.clickgui.window.ClickGuiWindow.Tooltip;
import org.bleachhack.setting.SettingDataHandlers;
import org.bleachhack.gui.clickgui.window.ModuleWindow;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;

public class SettingToggle extends ModuleSetting<Boolean> {

	protected final List<ModuleSetting<?>> children = new ArrayList<>();
	protected boolean expanded = false;

	public SettingToggle(String text, boolean state) {
		super(text, state, SettingDataHandlers.BOOLEAN);
	}
	
	public boolean getState() {
		return getValue().booleanValue();
	}

	public void render(ModuleWindow window, MatrixStack matrices, int x, int y, int len) {
		String color2 = getValue() ? "\u00a7a" : "\u00a7c";

		if (window.mouseOver(x, y, x + len, y + 12)) {
			DrawableHelper.fill(matrices, x + 1, y, x + len, y + 12, 0x70303070);
		}

		if (!children.isEmpty()) {
			if (window.rmDown && window.mouseOver(x, y, x + len, y + 12)) {
				expanded = !expanded;
				MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
			}

			if (expanded) {
				DrawableHelper.fill(matrices, x + 2, y + 12, x + 3, y + getHeight(len) - 1, 0xff8070b0);

				int h = y + 12;
				for (ModuleSetting<?> s : children) {
					s.render(window, matrices, x + 2, h, len - 2);

					h += s.getHeight(len - 3);
				}
			}

			if (expanded) {
				MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices,
						color2 + "\u2228",
						x + len - 8, y + 3, -1);
			} else {
				matrices.push();

				matrices.scale(0.75f, 0.75f, 1f);
				MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices,
						color2 + "\u00a7l>",
						(int) ((x + len - 7) * 1 / 0.75), (int) ((y + 4) * 1 / 0.75), -1);

				matrices.pop();
			}
		}

		MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, color2 + getName(), x + 3, y + 2, 0xffffff);

		if (window.mouseOver(x, y, x + len, y + 12) && window.lmDown) {
			setValue(!getValue());
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
		}
	}

	public int getHeight(int len) {
		int h = 12;

		if (expanded) {
			h += 1;
			for (ModuleSetting<?> s : children)
				h += s.getHeight(len - 2);
		}

		return h;
	}

	public ModuleSetting<?> getChild(int c) {
		return children.get(c);
	}

	public SettingToggle withChildren(ModuleSetting<?>... children) {
		this.children.addAll(Arrays.asList(children));
		return this;
	}

	public SettingToggle withDesc(String desc) {
		setTooltip(desc);
		return this;
	}

	public Tooltip getTooltip(ModuleWindow window, int x, int y, int len) {
		if (!expanded || window.mouseY - y <= 12)
			return super.getTooltip(window, x, y, len);

		Tooltip tooltip = null;

		int h = y + 12;
		for (ModuleSetting<?> s : children) {
			if (window.mouseOver(x + 2, h, x + len, h + s.getHeight(len))) {
				tooltip = s.getTooltip(window, x + 2, h, len - 2);
			}

			h += s.getHeight(len - 2);
		}

		return tooltip;
	}

	@Override
	public void read(JsonElement json) {
		if (json.isJsonObject()) {
			JsonObject jo = json.getAsJsonObject();
			if (jo.has("toggled")) {
				super.read(jo.get("toggled"));
			}

			for (Entry<String, JsonElement> e : jo.get("children").getAsJsonObject().entrySet()) {
				for (ModuleSetting<?> s : children) {
					if (s.getName().equals(e.getKey())) {
						s.read(e.getValue());
						break;
					}
				}
			}
		} else {
			super.read(json);
		}
	}

	@Override
	public JsonElement write() {
		if (children.isEmpty()) {
			return super.write();
		}

		JsonObject jo = new JsonObject();
		jo.add("toggled", super.write());

		JsonObject subJo = new JsonObject();
		for (ModuleSetting<?> s : children) {
			subJo.add(s.getName(), s.write());
		}

		jo.add("children", subJo);
		return jo;
	}

	@Override
	public boolean isDefault() {
		if (!super.isDefault())
			return false;

		for (ModuleSetting<?> s : children) {
			if (!s.isDefault())
				return false;
		}

		return true;
	}
}
