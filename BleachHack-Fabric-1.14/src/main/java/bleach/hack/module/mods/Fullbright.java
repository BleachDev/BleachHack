/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
 * Copyright (c) 2019 Bleach.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import com.google.common.eventbus.Subscribe;
import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.BleachQueue;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class Fullbright extends Module {
			
	public Fullbright() {
		super("Fullbright", GLFW.GLFW_KEY_C, Category.RENDER, "Turns your gamma setting up.",
				new SettingMode("Mode: ", "Gamma", "Potion", "Table"));
	}

	@Override
	public void onDisable() {
		super.onDisable();

		//mc.options.gamma = 1;
		if (mc.options.gamma > 1) {
			double g = mc.options.gamma;

			while (g > 1) {
				double nextStep = Math.max(g - 2, 1);
				BleachQueue.add("fullbright", () -> mc.options.gamma = nextStep);
				g -= 2;
			}
		}
		
		mc.player.removePotionEffect(StatusEffects.NIGHT_VISION);
		//Vanilla code to remap light level table.
		for (int i = 0; i <= 15; ++i) {
			float float_2 = 1.0F - (float)i / 15.0F;
			mc.world.dimension.getLightLevelToBrightness()[i] = (1.0F - float_2) / (float_2 * 3.0F + 1.0F) * 1.0F + 0.0F;
		}
	}
	
	public void onEnable() {
		super.onEnable();

		BleachQueue.cancelQueue("fullbright");
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (getSettings().get(0).asMode().mode == 0) {
			if (mc.options.gamma < 16) mc.options.gamma += 1.2;
		} else if (getSettings().get(0).asMode().mode == 1) {
			mc.player.addPotionEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1, 5));
		} else if (getSettings().get(0).asMode().mode == 2) {
			for (int i = 0; i < 16; i++) {
				if (mc.world.dimension.getLightLevelToBrightness()[i] != 1) {
					mc.world.dimension.getLightLevelToBrightness()[i] = 1;
				}
			}
		}
	}
}
