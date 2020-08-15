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

import bleach.hack.event.events.EventMovementTick;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSkyColor;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;

public class Ambience extends Module {

    public Ambience() {
        super("Ambience", KEY_UNBOUND, Category.WORLD, "Changes The World Time/Weather",
                new SettingToggle("Weather", true),
                new SettingToggle("Time", false),
                new SettingMode("Weather", "Clear", "Rain"),
                new SettingSlider("Rain", 0, 2, 0, 2),
                new SettingSlider("Time", 0, 24000, 12500, 0),
                new SettingToggle("Sky Color", false).withDesc("Custom color for the sky").withChildren(
                        new SettingColor("Sky Color", 0.6f, 0.1f, 0.7f, false).withDesc("Color for the sky")),
                new SettingToggle("Cloud Color", false).withDesc("Custom color for clouds").withChildren(
                        new SettingColor("Cloud Color", 0.8f, 0.2f, 1f, false).withDesc("Color for clouds")));
    }

    @Subscribe
    public void onPreTick(EventMovementTick event) {
        if (getSetting(0).asToggle().state) {
            if (getSetting(2).asMode().mode == 0) mc.world.setRainGradient(0f);
            else mc.world.setRainGradient((float) getSetting(3).asSlider().getValue());
        }
        if (getSetting(1).asToggle().state) {
            mc.world.setTimeOfDay((long) getSetting(4).asSlider().getValue());
            mc.world.setTimeOfDay((long) getSetting(4).asSlider().getValue());
        }
    }

    @Subscribe
    public void readPacket(EventReadPacket event) {
        if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
            event.setCancelled(true);
        }
    }

    @Subscribe
    public void onSkyColor(EventSkyColor event) {
        if (event instanceof EventSkyColor.CloudColor && getSetting(6).asToggle().state) {
            event.setColor(getSetting(6).asToggle().getChild(0).asColor().getRGBFloat());
        } else if (getSetting(5).asToggle().state) {
            event.setColor(getSetting(5).asToggle().getChild(0).asColor().getRGBFloat());
        }
    }
}
