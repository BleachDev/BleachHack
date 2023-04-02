/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventLightTex;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.util.BleachQueue;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class Fullbright extends Module {

	private float smooth;

	public Fullbright() {
		super("Fullbright", KEY_UNBOUND, ModuleCategory.RENDER, "Makes the world brighter.",
				new SettingMode("Mode", "Table", "Gamma", "Potion").withDesc("Fullbright mode."),
				new SettingSlider("Gamma", 1, 12, 9, 1).withDesc("How much to turn the gamma up when using gamma mode."));
	}

	@Override
	public void onDisable(boolean inWorld) {
		float g = smooth;
		while (g > 1f) {
			g = Math.max(g - 1.6f, 1);
			float nextStep = g;
			BleachQueue.add("fullbright", () -> {
				smooth = nextStep;
				System.out.println(nextStep + " > " + smooth);
				if (nextStep <= 1) {
					super.onDisable(inWorld);
				}
			});
		}

		if (inWorld)
			mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
	}

	@Override
	public void onEnable(boolean inWorld) {
		BleachQueue.runAllInQueue("fullbright");
		super.onEnable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (smooth < getSetting(1).asSlider().getValueFloat()) {
			smooth = Math.min(smooth + 1.6f, getSetting(1).asSlider().getValueFloat());
		} else if (smooth > getSetting(1).asSlider().getValueFloat()) {
			smooth = Math.max(smooth - 1.6f, getSetting(1).asSlider().getValueFloat());
		}

		if (getSetting(0).asMode().getMode() == 2) {
			mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 500, 0));
		}
	}

	@BleachSubscribe
	public void onBrightness(EventLightTex.Brightness event) {
		if (getSetting(0).asMode().getValue() == 0) {
			event.setBrightness((0.01f + event.getBrightness()) * smooth);
		}
	}

	@BleachSubscribe
	public void onGamma(EventLightTex.Gamma event) {
		if (getSetting(0).asMode().getValue() == 1) {
			event.setGamma(smooth);
		}
	}
}
