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
package bleach.hack.gui.clickgui.window;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Triple;

import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.setting.base.SettingBase;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;

public class ModuleWindow extends ClickGuiWindow {

	public List<Module> modList = new ArrayList<>();
	public LinkedHashMap<Module, Boolean> mods = new LinkedHashMap<>();

	public boolean hiding;

	private int len;

	private Set<Module> searchedModules;

	private Triple<Integer, Integer, String> tooltip = null;

	public ModuleWindow(List<Module> mods, int x1, int y1, int len, String title, ItemStack icon) {
		super(x1, y1, x1 + len, 0, title, icon);

		this.len = len;
		modList = mods;

		for (Module m : mods)
			this.mods.put(m, false);
		y2 = getHeight();
	}

	public void render(MatrixStack matrix, int mouseX, int mouseY) {
		super.render(matrix, mouseX, mouseY);

		TextRenderer textRend = mc.textRenderer;

		tooltip = null;
		int x = x1 + 1;
		int y = y1 + 13;
		x2 = x + len + 1;

		if (rmDown && mouseOver(x, y - 12, x + len, y)) {
			mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			hiding = !hiding;
		}

		if (hiding) {
			y2 = y;
			return;
		} else {
			y2 = y + getHeight();
		}

		int curY = 0;
		for (Entry<Module, Boolean> m : new LinkedHashMap<>(mods).entrySet()) {
			if (mouseOver(x, y + curY, x + len, y + 12 + curY)) {
				DrawableHelper.fill(matrix, x, y + curY, x + len, y + 12 + curY, 0x70303070);
			}

			textRend.drawWithShadow(matrix, textRend.trimToWidth(m.getKey().getName(), len),
					x + 2, y + 2 + curY, m.getKey().isEnabled() ? 0x70efe0 : 0xc0c0c0);

			// If they match: Module gets marked red
			if (searchedModules != null && searchedModules.contains(m.getKey()) && ModuleManager.getModule("ClickGui").getSetting(1).asToggle().state) {
				DrawableHelper.fill(matrix, x, y + curY, x + len, y + 12 + curY, 0x50ff0000);
			}

			// Set which module settings show on
			if (mouseOver(x, y + curY, x + len, y + 12 + curY)) {
				tooltip = Triple.of(x + len + 2, y + curY, m.getKey().getDesc());

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
				//horizonalGradient(matrix, x + 2, y + curY - 12, x + len - 2, y + curY - 11, 0xff6060b0, 0xff8070b0);
				
				for (SettingBase s : m.getKey().getSettings()) {
					s.render(this, matrix, x + 1, y + curY, len - 1);

					if (!s.getDesc().isEmpty() && mouseOver(x + 2, y + curY, x + len, y + s.getHeight(len) + curY)) {
						tooltip = s.getGuiDesc(this, x + 1, y + curY, len - 1);
					}

					//fillGreySides(matrix, x, y + curY - 1, x + len - 1, y + curY + s.getHeight(len));
					DrawableHelper.fill(matrix, x + 1, y + curY, x + 2, y + curY + s.getHeight(len), 0xff8070b0);

					curY += s.getHeight(len);
				}

				//horizonalGradient(matrix, x + 2, y + curY - 1, x + len - 2, y + curY, 0xff6060b0, 0xff8070b0);
			}
		}
	}

	protected void drawBar(MatrixStack matrix, int mouseX, int mouseY, TextRenderer textRend) {
		super.drawBar(matrix, mouseX, mouseY, textRend);
		textRend.draw(matrix, hiding ? "+" : "_", x2 - 10, y1 + (hiding ? 4 : 2), 0x000000);
		textRend.draw(matrix, hiding ? "+" : "_", x2 - 11, y1 + (hiding ? 3 : 1), 0xffffff);
	}

	public Triple<Integer, Integer, String> getTooltip() {
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
				for (SettingBase s : e.getKey().getSettings()) {
					h += s.getHeight(len);
				}
			}
		}

		return h;
	}
}
