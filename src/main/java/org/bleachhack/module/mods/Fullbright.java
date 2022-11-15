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
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.util.BleachQueue;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class Fullbright extends Module {

	public Fullbright() {
		super("Fullbright", KEY_UNBOUND, ModuleCategory.RENDER, "Makes the world brighter.",
				new SettingMode("Mode", "Gamma", "Potion").withDesc("Fullbright mode."),
				new SettingSlider("Gamma", 1, 12, 9, 1).withDesc("How much to turn the gamma up when using gamma mode."));
	}

	// table setting [B]roke

	@Override
	public void onDisable(boolean inWorld) {
		if (mc.options.getGamma().getValue() > 1) {
			double g = mc.options.getGamma().getValue();

			while (g > 1) {
				double nextStep = Math.max(g - 1.6, 1);
				BleachQueue.add("fullbright", () -> mc.options.getGamma().setValue(nextStep));
				g -= 1.6;
			}
		}

		if (inWorld)
			mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
		// Vanilla code to remap light level table.
		/* for (int i = 0; i <= 15; ++i) { float float_2 = 1.0F - (float)i / 15.0F;
		 * mc.world.dimension.getLightLevelToBrightness()[i] = (1.0F - float_2) /
		 * (float_2 * 3.0F + 1.0F) * 1.0F + 0.0F; } */

		super.onDisable(inWorld);
	}

	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);

		BleachQueue.cancelQueue("fullbright");
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (getSetting(0).asMode().getMode() == 0) {
			if (mc.options.getGamma().getValue() < getSetting(1).asSlider().getValue()) {
				mc.options.getGamma().setValue(Math.min(mc.options.getGamma().getValue() + 1, getSetting(1).asSlider().getValue()));
			} else if (mc.options.getGamma().getValue() > getSetting(1).asSlider().getValue()) {
				mc.options.getGamma().setValue(Math.max(mc.options.getGamma().getValue() - 1, getSetting(1).asSlider().getValue()));
			}
		} else if (getSetting(0).asMode().getMode() == 1) {
			mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 500, 0));
		} /* else if (getSetting(0).toMode().mode == 2) { for (int i = 0; i < 16; i++) {
		 * if (mc.world.dimension.getLightLevelToBrightness()[i] != 1) {
		 * mc.world.dimension.getLightLevelToBrightness()[i] = 1; } } } */
	}
}
