/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.setting.module;

public class SettingRotate extends SettingToggle {

	public SettingRotate(boolean state) {
		super("Rotate", state);
		setTooltip("Rotate server/clientside");
		children.add(new SettingMode("Mode", "Server", "Client").withDesc("How to rotate"));
	}

	public int getRotateMode() {
		return getChild(0).asMode().getValue();
	}
}
