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

import net.minecraft.util.Util;

public class StarGithub extends Module {

	public StarGithub() {
		super("StarGithub", KEY_UNBOUND, ModuleCategory.MISC, "I need to feed my 420 children pls star github.");
	}

	public void onEnable(boolean inWorld) {
		try {
			Util.getOperatingSystem().open("https://github.com/BleachDev/BleachHack");
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.setEnabled(false);
	}
}
