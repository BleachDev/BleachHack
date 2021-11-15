
/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.module.setting.base.SettingSlider;

public class Zoom extends Module {

	public double prevFov;
	public double prevSens;

	public Zoom() {
		super("Zoom", KEY_UNBOUND, ModuleCategory.RENDER, "ok zoomer.",
				new SettingSlider("Scale", 1, 10, 3, 2).withDesc("How much to zoom."));
	}

	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);

		prevFov = mc.options.fov;
		prevSens = mc.options.mouseSensitivity;

		mc.options.fov = prevFov / getSetting(0).asSlider().getValue();
		mc.options.mouseSensitivity = prevSens / getSetting(0).asSlider().getValue();
	}

	@Override
	public void onDisable(boolean inWorld) {
		mc.options.fov = prevFov;
		mc.options.mouseSensitivity = prevSens;

		super.onDisable(inWorld);
	}
}
