/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.gui.clickgui.window;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.ClickGui;
import org.bleachhack.setting.module.ModuleSetting;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;

public class ModuleWindow extends ClickGuiWindow {

	public List<Module> modList = new ArrayList<>();
	public LinkedHashMap<Module, Boolean> mods = new LinkedHashMap<>();

	private int len;

	private Set<Module> searchedModules;

	private Tooltip tooltip = null;

	public ModuleWindow(List<Module> mods, int x1, int y1, int len, String title, ItemStack icon) {
		super(x1, y1, x1 + len, 0, title, icon);

		this.len = len;
		modList = mods;

		for (Module m : mods)
			this.mods.put(m, false);

		y2 = getHeight();
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY) {
		tooltip = null;
		int x = x1 + 1;
		int y = y1 + 13;
		x2 = x + len + 1;
		y2 = hiding ? y1 + 13 : y1 + 13 + getHeight();

		super.render(matrices, mouseX, mouseY);

		if (hiding) return;

		TextRenderer textRend = mc.textRenderer;

		int curY = 0;
		for (Entry<Module, Boolean> m : mods.entrySet()) {
			if (mouseOver(x, y + curY, x + len, y + 12 + curY)) {
				DrawableHelper.fill(matrices, x, y + curY, x + len, y + 12 + curY, 0x70303070);
			}

			// If they match: Module gets marked red
			if (searchedModules != null && searchedModules.contains(m.getKey()) && ModuleManager.getModule(ClickGui.class).getSetting(1).asToggle().getState()) {
				DrawableHelper.fill(matrices, x, y + curY, x + len, y + 12 + curY, 0x50ff0000);
			}

			textRend.drawWithShadow(matrices, textRend.trimToWidth(m.getKey().getName(), len),
					x + 2, y + 2 + curY, m.getKey().isEnabled() ? 0x70efe0 : 0xc0c0c0);

			// Set which module settings show on
			if (mouseOver(x, y + curY, x + len, y + 12 + curY)) {
				tooltip = new Tooltip(x + len + 2, y + curY, m.getKey().getDesc());

				if (lmDown)
					m.getKey().toggle();
				if (rmDown)
					mods.replace(m.getKey(), !m.getValue());
				if (lmDown || rmDown)
					mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			}

			curY += 12;

			// draw settings
			if (m.getValue()) {
				for (ModuleSetting<?> s : m.getKey().getSettings()) {
					s.render(this, matrices, x + 1, y + curY, len - 1);

					if (!s.getTooltip().isEmpty() && mouseOver(x + 2, y + curY, x + len, y + s.getHeight(len) + curY)) {
						tooltip = s.getTooltip(this, x + 1, y + curY, len - 1);
					}

					DrawableHelper.fill(matrices, x + 1, y + curY, x + 2, y + curY + s.getHeight(len), 0xff8070b0);

					curY += s.getHeight(len);
				}
			}
		}
	}

	public Tooltip getTooltip() {
		return tooltip;
	}

	public void setSearchedModule(Set<Module> mods) {
		searchedModules = mods;
	}

	public void setLen(int len) {
		this.len = len;
	}

	public int getHeight() {
		int h = 1;
		for (Entry<Module, Boolean> e : mods.entrySet()) {
			h += 12;

			if (e.getValue()) {
				for (ModuleSetting<?> s : e.getKey().getSettings()) {
					h += s.getHeight(len);
				}
			}
		}

		return h;
	}
}
