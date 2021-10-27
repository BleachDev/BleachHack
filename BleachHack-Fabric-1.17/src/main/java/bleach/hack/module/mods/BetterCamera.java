/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingSlider;
import bleach.hack.module.setting.base.SettingToggle;
import bleach.hack.module.Module;

public class BetterCamera extends Module {

	public BetterCamera() {
		super("BetterCamera", KEY_UNBOUND, ModuleCategory.RENDER, "Improves the 3rd person camera.",
				new SettingToggle("CameraClip", true).withDesc("Makes the camera clip into walls."),
				new SettingToggle("Distance", true).withDesc("Sets a custom camera distance.").withChildren(
						new SettingSlider("Distance", 0.5, 15, 4, 1).withDesc("The desired camera distance.")));
	}

	// Logic handled in MixinCamera::clipToSpace
}
