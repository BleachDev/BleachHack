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

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventMovementTick;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.client.network.packet.WorldTimeUpdateS2CPacket;

public class Ambience extends Module {
	
	public Ambience() {
		super("Ambience", -1, Category.WORLD, "Changes The World Time/Weather",
				new SettingToggle("Weather", true),
				new SettingToggle("Time", false),
				new SettingMode("Weather: ", "Clear", "Rain"),
				new SettingSlider("Rain: ", 0, 2, 0, 2),
				new SettingSlider("Time: ", 0, 24000, 12500, 0));
	}
	
	@Subscribe
	public void onPreTick(EventMovementTick event) {
		if(getSettings().get(0).toToggle().state) {
			if(getSettings().get(2).toMode().mode == 0) mc.world.setRainGradient(0f);
			else mc.world.setRainGradient((float) getSettings().get(3).toSlider().getValue());
		}
		if(getSettings().get(1).toToggle().state) {
			mc.world.setTime((long) getSettings().get(4).toSlider().getValue());
			mc.world.setTimeOfDay((long) getSettings().get(4).toSlider().getValue());
		}
	}
	
	@Subscribe
	public void readPacket(EventReadPacket event) {
		if(event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
			event.setCancelled(true);
		}
	}

}
