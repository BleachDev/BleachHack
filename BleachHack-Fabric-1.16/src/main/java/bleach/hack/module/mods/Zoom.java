
/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.BleachQueue;

public class Zoom extends Module {

	public double prevFov;
	public double prevSens;

	public Zoom() {
		super("Zoom", KEY_UNBOUND, Category.RENDER, "ok zoomer",
				new SettingSlider("Scale", 1, 10, 3, 2).withDesc("How much to zoom"),
				new SettingToggle("Smooth", false).withDesc("Smooths the zoom when enabling and disabling"));
	}

	public void onEnable() {
		super.onEnable();
		BleachQueue.runAllInQueue("zoom");

		prevFov = mc.options.fov;
		prevSens = mc.options.mouseSensitivity;

		if (getSetting(0).asSlider().getValue() < 1 || !getSetting(1).asToggle().state) {
			mc.options.fov = prevFov / getSetting(0).asSlider().getValue();
			mc.options.mouseSensitivity = prevSens / getSetting(0).asSlider().getValue();
		}
	}

	public void onDisable() {
		if (getSetting(0).asSlider().getValue() < 1 || !getSetting(1).asToggle().state) {
			mc.options.fov = prevFov;
			mc.options.mouseSensitivity = prevSens;
		} else {
			for (double f = mc.options.fov; f < prevFov; f *= 1.4) {
				BleachQueue.add("zoom", () -> {
					mc.options.fov = Math.min(prevFov, mc.options.fov * 1.4);
					mc.options.mouseSensitivity = Math.min(prevSens, mc.options.mouseSensitivity * 1.4);
				});
			}
		}

		super.onDisable();
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (getSetting(0).asSlider().getValue() >= 1 && mc.options.fov > prevFov / getSetting(0).asSlider().getValue()) {
			mc.options.fov /= 1.4;
			mc.options.mouseSensitivity /= 1.4;

			if (mc.options.fov <= prevFov / getSetting(0).asSlider().getValue()) {
				mc.options.fov = prevFov / getSetting(0).asSlider().getValue();
				mc.options.mouseSensitivity = prevSens / getSetting(0).asSlider().getValue();
			}
		} else {
			mc.options.fov = prevFov / getSetting(0).asSlider().getValue();
			mc.options.mouseSensitivity = prevSens / getSetting(0).asSlider().getValue();
		}
	}
}
