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

import java.util.Arrays;
import java.util.List;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

public class SpeedMine extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"1", "2", "3"}, "Haste: "));
	
	public SpeedMine() {
		super("SpeedMine", -1, Category.EXPLOITS, "Allows you to mine at sanic speeds", settings);
	}
	
	public void onUpdate() {
		if (this.isToggled()) {
			mc.player.addPotionEffect(new EffectInstance(Effects.HASTE, 1, getSettings().get(0).toMode().mode));
		}
	}
	
	public void onDisable() {
		mc.player.removeActivePotionEffect(Effects.HASTE);
	}

}
