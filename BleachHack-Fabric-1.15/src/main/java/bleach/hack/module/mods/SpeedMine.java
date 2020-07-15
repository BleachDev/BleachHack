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
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class SpeedMine extends Module {

	public SpeedMine() {
		super("SpeedMine", KEY_UNBOUND, Category.EXPLOITS, "Allows you to mine at sanic speeds",
				new SettingMode("Mode: ", "Haste", "OG"),
				new SettingSlider("Haste Lvl: ", 1, 3, 1, 0),
				new SettingSlider("Cooldown: ", 0, 4, 1, 0),
				new SettingSlider("Multiplier: ", 1, 3, 1.3, 1));
	}

	@Override
	public void onDisable() {
		super.onDisable();
		mc.player.removeStatusEffect(StatusEffects.HASTE);
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (this.getSettings().get(0).asMode().mode == 0) {
			mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 1, (int) getSettings().get(1).asSlider().getValue()));
		}
	}
}
