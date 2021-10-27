/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import bleach.hack.eventbus.BleachSubscribe;
import bleach.hack.event.events.EventReach;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingSlider;
import bleach.hack.module.Module;

public class Reach extends Module {

	public Reach() {
		super("Reach", KEY_UNBOUND, ModuleCategory.PLAYER, "Turns you into long armed popbob.",
				new SettingSlider("Reach", 0, 1, 0.3, 2).withDesc("How much further to be able to reach."));
	}

	@BleachSubscribe
	public void onReach(EventReach event) {
		event.setReach(event.getReach() + getSetting(0).asSlider().getValueFloat());
	}
}
