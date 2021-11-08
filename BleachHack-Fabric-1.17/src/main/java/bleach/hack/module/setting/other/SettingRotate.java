/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.setting.other;

import bleach.hack.module.setting.base.SettingMode;
import bleach.hack.module.setting.base.SettingToggle;

public class SettingRotate extends SettingToggle {

	public SettingRotate(boolean state) {
		super("Rotate", state);
		description = "Rotate server/clientside";
		children.add(new SettingMode("Mode", "Server", "Client").withDesc("How to rotate"));
	}

	public int getRotateMode() {
		return getChild(0).asMode().mode;
	}
}
