/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingToggle;

public class Sprint extends Module {

	public Sprint() {
		super("Sprint", KEY_UNBOUND, ModuleCategory.MOVEMENT, "Makes the player automatically sprint.",
				new SettingToggle("HungerCheck", false).withDesc("Checks that you actually have enough hunger to sprint."));
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (getSetting(0).asToggle().getState() && mc.player.getHungerManager().getFoodLevel() <= 6)
			return;

		mc.player.setSprinting(
				mc.player.input.movementForward > 0 && 
				(mc.player.input.movementSideways != 0 ||mc.player.input.movementForward > 0) &&
				!mc.player.isSneaking());
	}
}
