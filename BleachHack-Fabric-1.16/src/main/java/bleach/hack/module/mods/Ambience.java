/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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

import bleach.hack.event.events.EventBiomeColor;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSkyRender;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import net.minecraft.client.render.SkyProperties;
import net.minecraft.client.render.SkyProperties.SkyType;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Ambience extends Module {

	public Ambience() {
		super("Ambience", KEY_UNBOUND, Category.WORLD, "Changes the world ambience",
				new SettingToggle("Weather", true).withDesc("Changes the world weather").withChildren(
						new SettingMode("Weather", "Clear", "Rain").withDesc("What weather to use"),
						new SettingSlider("Rain", 0, 2, 0, 2).withDesc("How much rain")),
				new SettingToggle("Time", false).withDesc("Changes the world time").withChildren(
						new SettingSlider("Time", 0, 24000, 12500, 0).withDesc("What time to use")),
				new SettingToggle("Overworld", true).withDesc("Changes the overworld ambience").withChildren(
						new SettingToggle("Sky Color", true).withDesc("Changes the overworld sky color").withChildren(
								new SettingToggle("End Skybox", false).withDesc("2B2T QUeue SKY=!?!?!?"),
								new SettingColor("Sky Color", 0.5f, 1f, 0.5f, false).withDesc("Main color of the sky")),
						new SettingToggle("Foilage Color", false).withDesc("Changes the foilage color").withChildren(
								new SettingColor("Color", 0.5f, 1f, 0.5f, false).withDesc("Foilage color")),
						new SettingToggle("Water Color", false).withDesc("Changes the water color").withChildren(
								new SettingColor("Color", 0.5f, 1f, 0.5f, false).withDesc("Water color"))),
				new SettingToggle("Nether", true).withDesc("Changes the nether ambience").withChildren(
						new SettingToggle("Sky Color", true).withDesc("Changes the nether sky color").withChildren(
								new SettingToggle("End Skybox", false).withDesc("2B2T QUeue SKY=!?!?!?"),
								new SettingColor("Sky Color", 0.5f, 1f, 0.5f, false).withDesc("Main color of the sky")),
						new SettingToggle("Foilage Color", false).withDesc("Changes the foilage color").withChildren(
								new SettingColor("Color", 0.5f, 1f, 0.5f, false).withDesc("Foilage color")),
						new SettingToggle("Water Color", false).withDesc("Changes the water color").withChildren(
								new SettingColor("Color", 0.5f, 1f, 0.5f, false).withDesc("Water color"))),
				new SettingToggle("End", true).withDesc("Changes the end ambience").withChildren(
						new SettingToggle("Sky Color", true).withDesc("Changes the end sky color").withChildren(
								new SettingToggle("End Skybox", false).withDesc("2B2T QUeue SKY=!?!?!?"),
								new SettingColor("Sky Color", 0.5f, 1f, 0.5f, false).withDesc("Main color of the sky")),
						new SettingToggle("Foilage Color", false).withDesc("Changes the foilage color").withChildren(
								new SettingColor("Color", 0.5f, 1f, 0.5f, false).withDesc("Foilage color")),
						new SettingToggle("Water Color", false).withDesc("Changes the water color").withChildren(
								new SettingColor("Color", 0.5f, 1f, 0.5f, false).withDesc("Water color"))));
	}

	public SkyProperties getCurrentSky() {
		return new SkyProperties(128f, false, SkyType.NORMAL, false, false) {

			@Override
			public boolean useThickFog(int camouseX, int camouseY) {
				return false;
			}

			@Override
			public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
				return color;
			}
		};
	}

	@Subscribe
	public void onPreTick(EventTick event) {
		if (getSetting(0).asToggle().state) {
			if (getSetting(0).asToggle().getChild(0).asMode().mode == 0) {
				mc.world.setRainGradient(0f);
			} else {
				mc.world.setRainGradient((float) getSetting(0).asToggle().getChild(1).asSlider().getValue());
			}
		}

		if (getSetting(1).asToggle().state) {
			mc.world.setTimeOfDay((long) getSetting(1).asToggle().getChild(0).asSlider().getValue());
		}
	}

	@Subscribe
	public void readPacket(EventReadPacket event) {
		if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
			event.setCancelled(true);
		}
	}

	@Subscribe
	public void onBiomeColor(EventBiomeColor event) {
		int type = event instanceof EventBiomeColor.Water ? 2 : 1;

		event.setColor(getCurrentDimSetting().state && getCurrentDimSetting().getChild(type).asToggle().state
				? getCurrentDimSetting().getChild(type).asToggle().getChild(0).asColor().getRGB() : null);
	}

	@Subscribe
	public void onSkyColor(EventSkyRender.Color event) {
		if (getCurrentDimSetting().state && getCurrentDimSetting().getChild(0).asToggle().state) {
			event.setColor(getCurrentDimSetting().getChild(0).asToggle().getChild(1).asColor().getRGBFloat());
		}
	}

	@Subscribe
	public void onSkyProperties(EventSkyRender.Properties event) {
		if (getCurrentDimSetting().state && getCurrentDimSetting().getChild(0).asToggle().state
				&& getCurrentDimSetting().getChild(0).asToggle().getChild(0).asToggle().state) {
			event.setSky(new SkyProperties(event.getSky().getCloudsHeight(), false, SkyProperties.SkyType.END, true, false) {

				public Vec3d adjustFogColor(Vec3d color, float sunHeight) {
					return color.multiply(0.15000000596046448D);
				}

				public boolean useThickFog(int camouseX, int camouseY) {
					return false;
				}

				public float[] getFogColorOverride(float skyAngle, float tickDelta) {
					return null;
				}
			});
		}
	}

	private SettingToggle getCurrentDimSetting() {
		return getSetting(mc.world.getRegistryKey() == World.END ? 4 : mc.world.getRegistryKey() == World.NETHER ? 3 : 2).asToggle();
	}
}
