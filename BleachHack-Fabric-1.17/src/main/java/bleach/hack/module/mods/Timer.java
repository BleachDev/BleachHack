/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;

public class Timer extends Module {

	public Timer() {
		super("Timer", KEY_UNBOUND, Category.WORLD, "more speeds",
				new SettingSlider("Speed", 0.01, 20, 1, 2).withDesc("How fast to set timer"));
	}

	// See MixinRenderTickCounter for code

}
