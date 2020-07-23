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

public abstract class SettingBase {

	protected String description = "";

	public SettingMode asMode() {
		try {
			return (SettingMode) this;
		} catch (Exception e) {
			throw new IllegalStateException("Execption parsing setting: " + this);
		}
	}

	public SettingToggle asToggle() {
		try {
			return (SettingToggle) this;
		} catch (Exception e) {
			throw new IllegalStateException("Execption parsing setting: " + this);
		}
	}

	public SettingSlider asSlider() {
		try {
			return (SettingSlider) this;
		} catch (Exception e) {
			throw new IllegalStateException("Execption parsing setting: " + this);
		}
	}
	
	public abstract String getName();

	public String getDesc() {
		return description;
	}

	public SettingBase withDesc(String desc) {
		description = desc;
		return this;
	}
	
	public abstract void readSettings(JsonElement settings);
	public abstract JsonElement saveSettings();
}
