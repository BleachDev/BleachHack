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
 
 package bleach.hack.module.mods;

import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;

public class Zoom extends Module {

	public Zoom() {
		super("Zoom", KEY_UNBOUND, Category.RENDER, "zooms",
				new SettingSlider("Scale: ", 1, 10, 3, 1));
	}

	public double prevFov;
	public double prevSens;
	public void onEnable() {
		prevFov = mc.options.fov;
		mc.options.fov = prevFov / getSetting(0).asSlider().getValue();
		prevSens = mc.options.mouseSensitivity;
		mc.options.mouseSensitivity = prevSens / getSetting(0).asSlider().getValue();
		super.onEnable();
	}
	public void onDisable() {
		mc.options.fov = prevFov;
		mc.options.mouseSensitivity = prevSens;
		super.onDisable();
	}
}
