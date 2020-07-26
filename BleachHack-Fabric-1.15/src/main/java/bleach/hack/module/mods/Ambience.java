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
import bleach.hack.event.events.EventSkyColor;
import bleach.hack.gui.clickgui.SettingColor;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class Ambience extends Module {

	public Ambience() {
		super("Ambience", KEY_UNBOUND, Category.WORLD, "Changes The World Time/Weather",
				new SettingToggle("Weather", true),
				new SettingToggle("Time", false),
				new SettingMode("Weather: ", "Clear", "Rain"),
				new SettingSlider("Rain: ", 0, 2, 0, 2),
				new SettingSlider("Time: ", 0, 24000, 12500, 0),
				
				new SettingToggle("Sky Color", false).withChildren(
						new SettingColor("Sky Color", 0.6f, 0.1f, 0.7f, false).withDesc("Color for the sky"))
				.withDesc("Custom color for the sky"),
				
				new SettingToggle("Cloud Color", false).withChildren(
						new SettingColor("Cloud Color", 0.8f, 0.2f, 1f, false).withDesc("Color for clouds"))
				.withDesc("Custom color for clouds"));
	}

	@Subscribe
	public void onPreTick(EventMovementTick event) {
		if (getSettings().get(0).asToggle().state) {
			if (getSettings().get(2).asMode().mode == 0) mc.world.setRainGradient(0f);
			else mc.world.setRainGradient((float) getSettings().get(3).asSlider().getValue());
		}
		if (getSettings().get(1).asToggle().state) {
			mc.world.setTime((long) getSettings().get(4).asSlider().getValue());
			mc.world.setTimeOfDay((long) getSettings().get(4).asSlider().getValue());
		}
	}

	@Subscribe
	public void readPacket(EventReadPacket event) {
		if (getSettings().get(1).asToggle().state && event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
			event.setCancelled(true);
		}
	}
	
	@Subscribe
	public void onSkyColor(EventSkyColor event) {
		if (event instanceof EventSkyColor.CloudColor && getSettings().get(6).asToggle().state) {
			event.setColor(getSettings().get(6).getChild(0).asColor().getRGBFloat());
		} else if (getSettings().get(5).asToggle().state) {
			event.setColor(getSettings().get(5).getChild(0).asColor().getRGBFloat());
		}
	}

}
