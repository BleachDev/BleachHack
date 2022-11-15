
/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingSlider;

public class Zoom extends Module {

	public int prevFov;
	public double prevSens;

	public Zoom() {
		super("Zoom", KEY_UNBOUND, ModuleCategory.RENDER, "ok zoomer.",
				new SettingSlider("Scale", 1, 10, 3, 2).withDesc("How much to zoom."));
	}

	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);

		prevFov = mc.options.getFov().getValue();
		prevSens = mc.options.getMouseSensitivity().getValue();

		mc.options.getFov().setValue((int) (prevFov / getSetting(0).asSlider().getValue()));
		mc.options.getMouseSensitivity().setValue(prevSens / getSetting(0).asSlider().getValue());
	}

	@Override
	public void onDisable(boolean inWorld) {
		mc.options.getFov().setValue(prevFov);
		mc.options.getMouseSensitivity().setValue(prevSens);

		super.onDisable(inWorld);
	}
}
