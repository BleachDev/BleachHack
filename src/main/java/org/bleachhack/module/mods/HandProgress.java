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
import org.bleachhack.mixin.AccessorHeldItemRenderer;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;

public class HandProgress extends Module {

	public HandProgress() {
		super("HandProgress", KEY_UNBOUND, ModuleCategory.RENDER, "Smaller view of your mainhand/offhand.",
				new SettingSlider("Mainhand", 0.1, 1.0, 1.0, 1).withDesc("Main hand size."), // 0
				new SettingSlider("Offhand", 0.1, 1.0, 1.0, 1).withDesc("Offhand size."), // 1
				new SettingToggle("NoAnimation", false).withDesc("Removes the animation when swapping items.")
				);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		AccessorHeldItemRenderer accessor = (AccessorHeldItemRenderer) mc.gameRenderer.firstPersonRenderer;

		// Refresh the item held in hand every tick
		accessor.setMainHand(mc.player.getMainHandStack());
		accessor.setOffHand(mc.player.getOffHandStack());

		// Set the item render height
		float mainHand = getSetting(2).asToggle().getState() 
				? getSetting(0).asSlider().getValueFloat()
						: Math.min(accessor.getEquipProgressMainHand(), getSetting(0).asSlider().getValueFloat());

		float offHand = getSetting(2).asToggle().getState() 
				? getSetting(1).asSlider().getValueFloat()
						: Math.min(accessor.getEquipProgressOffHand(), getSetting(1).asSlider().getValueFloat());
		
		accessor.setEquipProgressMainHand(mainHand);
		accessor.setEquipProgressOffHand(offHand);
	}
}
