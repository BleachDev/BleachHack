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

public class Timer extends Module {

	public Timer() {
		super("Timer", KEY_UNBOUND, ModuleCategory.WORLD, "Speeds up the world clientside.",
				new SettingSlider("Speed", 0.01, 20, 1, 2).withDesc("How fast to tick the world."));
	}

	// See MixinRenderTickCounter for code

}
