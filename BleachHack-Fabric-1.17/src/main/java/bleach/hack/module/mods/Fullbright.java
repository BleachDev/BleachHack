/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import org.lwjgl.glfw.GLFW;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.util.BleachQueue;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class Fullbright extends Module {

	public Fullbright() {
		super("Fullbright", GLFW.GLFW_KEY_C, Category.RENDER, "Turns your gamma setting up.",
				new SettingMode("Mode", "Gamma", "Potion").withDesc("Fullbright mode"),
				new SettingSlider("Gamma", 1, 12, 9, 1).withDesc("How much to turn the gamma up when using gamma mode"));
	}

	// table setting [B]roke

	@Override
	public void onDisable() {
		super.onDisable();

		if (mc.options.gamma > 1) {
			double g = mc.options.gamma;

			while (g > 1) {
				double nextStep = Math.max(g - 1.6, 1);
				BleachQueue.add("fullbright", () -> mc.options.gamma = nextStep);
				g -= 1.6;
			}
		}

		mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
		// Vanilla code to remap light level table.
		/* for (int i = 0; i <= 15; ++i) { float float_2 = 1.0F - (float)i / 15.0F;
		 * mc.world.dimension.getLightLevelToBrightness()[i] = (1.0F - float_2) /
		 * (float_2 * 3.0F + 1.0F) * 1.0F + 0.0F; } */
	}

	public void onEnable() {
		super.onEnable();

		BleachQueue.cancelQueue("fullbright");
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (getSetting(0).asMode().mode == 0) {
			if (mc.options.gamma < getSetting(1).asSlider().getValue()) {
				mc.options.gamma = Math.min(mc.options.gamma + 1, getSetting(1).asSlider().getValue());
			} else if (mc.options.gamma > getSetting(1).asSlider().getValue()) {
				mc.options.gamma = Math.max(mc.options.gamma - 1, getSetting(1).asSlider().getValue());
			}
		} else if (getSetting(0).asMode().mode == 1) {
			mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 500, 0));
		} /* else if (getSetting(0).toMode().mode == 2) { for (int i = 0; i < 16; i++) {
		 * if (mc.world.dimension.getLightLevelToBrightness()[i] != 1) {
		 * mc.world.dimension.getLightLevelToBrightness()[i] = 1; } } } */
	}
}
