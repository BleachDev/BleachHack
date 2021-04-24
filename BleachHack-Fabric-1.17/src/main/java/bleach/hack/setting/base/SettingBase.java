/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.setting.base;

import org.apache.commons.lang3.tuple.Triple;

import com.google.gson.JsonElement;

import bleach.hack.gui.clickgui.window.ModuleWindow;
import bleach.hack.setting.other.SettingRotate;
import net.minecraft.client.util.math.MatrixStack;

public abstract class SettingBase {

	protected String description = "";

	public SettingMode asMode() {
		try {
			return (SettingMode) this;
		} catch (Exception e) {
			throw new ClassCastException("Execption parsing setting: " + this);
		}
	}

	public SettingToggle asToggle() {
		try {
			return (SettingToggle) this;
		} catch (Exception e) {
			throw new ClassCastException("Execption parsing setting: " + this);
		}
	}

	public SettingSlider asSlider() {
		try {
			return (SettingSlider) this;
		} catch (Exception e) {
			throw new ClassCastException("Execption parsing setting: " + this);
		}
	}

	public SettingColor asColor() {
		try {
			return (SettingColor) this;
		} catch (Exception e) {
			throw new ClassCastException("Execption parsing setting: " + this);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <E> SettingList<E> asList(Class<E> itemClass) {
		try {
			return (SettingList<E>) this;
		} catch (Exception e) {
			throw new ClassCastException("Execption parsing setting: " + this);
		}
	}

	public SettingRotate asRotate() {
		try {
			return (SettingRotate) this;
		} catch (Exception e) {
			throw new ClassCastException("Execption parsing setting: " + this);
		}
	}
	
	public SettingBind asBind() {
		try {
			return (SettingBind) this;
		} catch (Exception e) {
			throw new ClassCastException("Execption parsing setting: " + this);
		}
	}

	public abstract String getName();

	public String getDesc() {
		return description;
	}

	public Triple<Integer, Integer, String> getGuiDesc(ModuleWindow window, int x, int y, int len) {
		return Triple.of(x + len + 2, y, description);
	}

	public abstract void render(ModuleWindow window, MatrixStack matrix, int x, int y, int len);

	public abstract int getHeight(int len);

	public abstract void readSettings(JsonElement settings);

	public abstract JsonElement saveSettings();

	public abstract boolean isDefault();
}
