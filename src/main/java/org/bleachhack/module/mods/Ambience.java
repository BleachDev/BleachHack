/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventBiomeColor;
import org.bleachhack.event.events.EventPacket;
import org.bleachhack.event.events.EventSkyRender;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingColor;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;

import net.minecraft.client.render.DimensionEffects;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Ambience extends Module {

	private final WeatherManager weatherManager = new WeatherManager();

	public Ambience() {
		super("Ambience", KEY_UNBOUND, ModuleCategory.WORLD, "Changes the world ambience.",
				new SettingToggle("Weather", true).withDesc("Changes the world weather.").withChildren(
						new SettingMode("Weather", "Clear", "Rain").withDesc("What weather to use."),
						new SettingSlider("Rain", 0, 2, 0, 2).withDesc("How much it should rain in rain mode.")),
				new SettingToggle("Time", false).withDesc("Changes the world time.").withChildren(
						new SettingSlider("Time", 0, 24000, 12500, 0).withDesc("What time to set the world to.")),
				new SettingToggle("Overworld", true).withDesc("Changes the overworld ambience-").withChildren(
						new SettingToggle("Sky Color", true).withDesc("Changes the overworld sky color.").withChildren(
								new SettingToggle("End Skybox", false).withDesc("2B2T QUeue SKY=!?!?!?"),
								new SettingColor("Sky Color", 128, 255, 128).withDesc("Main color of the sky.")),
						new SettingToggle("Foilage Color", false).withDesc("Changes the foilage color.").withChildren(
								new SettingColor("Color", 128, 255, 128).withDesc("The color of the foilage.")),
						new SettingToggle("Water Color", false).withDesc("Changes the water color.").withChildren(
								new SettingColor("Color", 128, 255, 128).withDesc("Color of the water."))),
				new SettingToggle("Nether", true).withDesc("Changes the nether ambience.").withChildren(
						new SettingToggle("Sky Color", true).withDesc("Changes the nether sky color.").withChildren(
								new SettingToggle("End Skybox", false).withDesc("2B2T QUeue SKY=!?!?!?"),
								new SettingColor("Sky Color", 128, 255, 128).withDesc("Main color of the sky.")),
						new SettingToggle("Foilage Color", false).withDesc("Changes the foilage color.").withChildren(
								new SettingColor("Color", 128, 255, 128).withDesc("The color of the foilage.")),
						new SettingToggle("Water Color", false).withDesc("Changes the water color").withChildren(
								new SettingColor("Color", 128, 255, 128).withDesc("The color of the water."))),
				new SettingToggle("End", true).withDesc("Changes the end ambience.").withChildren(
						new SettingToggle("Sky Color", true).withDesc("Changes the end sky color.").withChildren(
								new SettingToggle("End Skybox", false).withDesc("2B2T QUeue SKY=!?!?!?"),
								new SettingColor("Sky Color", 128, 255, 128).withDesc("Main color of the sky.")),
						new SettingToggle("Foilage Color", false).withDesc("Changes the foilage color.").withChildren(
								new SettingColor("Color", 128, 255, 128).withDesc("The color of the foilage.")),
						new SettingToggle("Water Color", false).withDesc("Changes the water color.").withChildren(
								new SettingColor("Color", 128, 255, 128).withDesc("The color of the water."))));
	}

	@Override
	public void onDisable(boolean inWorld) {
		if (inWorld)
			weatherManager.applyWeather(mc.world);

		weatherManager.reset();

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (getSetting(0).asToggle().getState()) {
			if (!weatherManager.isActive()) {
				weatherManager.setRain(mc.world.getRainGradient(mc.getTickDelta()));
				weatherManager.setThunder(mc.world.getThunderGradient(mc.getTickDelta()));
			}

			if (getSetting(0).asToggle().getChild(0).asMode().getMode() == 0) {
				mc.world.getLevelProperties().setRaining(false);
				mc.world.setRainGradient(0f);
			} else {
				mc.world.getLevelProperties().setRaining(true);
				mc.world.setRainGradient(getSetting(0).asToggle().getChild(1).asSlider().getValueFloat());
			}
		} else if (weatherManager.isActive()) {
			weatherManager.applyWeather(mc.world);
			weatherManager.reset();
		}

		if (getSetting(1).asToggle().getState()) {
			mc.world.setTimeOfDay(getSetting(1).asToggle().getChild(0).asSlider().getValueLong());
		}
	}

	@BleachSubscribe
	public void readPacket(EventPacket.Read event) {
		if (event.getPacket() instanceof GameStateChangeS2CPacket && getSetting(0).asToggle().getState()) {
			GameStateChangeS2CPacket packet = (GameStateChangeS2CPacket) event.getPacket();
			if (packet.getReason() == GameStateChangeS2CPacket.RAIN_STARTED) {
				weatherManager.setRain(1f);
			} else if (packet.getReason() == GameStateChangeS2CPacket.RAIN_STOPPED) {
				weatherManager.setRain(0f);
			} else if (packet.getReason() == GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED) {
				weatherManager.setRain(packet.getValue());
			} else if (packet.getReason() == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED) {
				weatherManager.setThunder(packet.getValue());
			} else {
				return;
			}

			event.setCancelled(true);
		} else if (event.getPacket() instanceof DisconnectS2CPacket && getSetting(0).asToggle().getState()) {
			weatherManager.reset();
		} else if (event.getPacket() instanceof WorldTimeUpdateS2CPacket && getSetting(1).asToggle().getState()) {
			event.setCancelled(true);
		}
	}

	@BleachSubscribe
	public void onBiomeColor(EventBiomeColor event) {
		int type = event instanceof EventBiomeColor.Water ? 2 : 1;

		if (getCurrentDimSetting().getState() && getCurrentDimSetting().getChild(type).asToggle().getState()) {
			event.setColor(getCurrentDimSetting().getChild(type).asToggle().getChild(0).asColor().getRGB());
		}
	}

	@BleachSubscribe
	public void onSkyColor(EventSkyRender.Color event) {
		if (getCurrentDimSetting().getState() && getCurrentDimSetting().getChild(0).asToggle().getState()) {
			int[] color = getCurrentDimSetting().getChild(0).asToggle().getChild(1).asColor().getRGBArray();
			event.setColor(new Vec3d(color[0] / 255d, color[1] / 255d, color[2] / 255d));
		}
	}

	@BleachSubscribe
	public void onSkyProperties(EventSkyRender.Properties event) {
		if (getCurrentDimSetting().getState() && getCurrentDimSetting().getChild(0).asToggle().getState()
				&& getCurrentDimSetting().getChild(0).asToggle().getChild(0).asToggle().getState()) {
			event.setSky(new DimensionEffects(event.getSky().getCloudsHeight(), false, DimensionEffects.SkyType.END, true, false) {

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

	private static class WeatherManager {

		private float rain = -1f;
		private float thunder = -1f;

		public void setRain(float rain) {
			this.rain = rain;
		}

		public void setThunder(float thunder) {
			this.thunder = thunder;
		}

		public void reset() {
			rain = -1f;
			thunder = -1f;
		}

		public void applyWeather(World world) {
			if (rain >= 0f) {
				world.getLevelProperties().setRaining(rain > 0f);
				world.setRainGradient(rain);
			}

			if (thunder >= 0f) {
				world.setThunderGradient(thunder);
			}
		}

		public boolean isActive() {
			return rain >= 0f || thunder >= 1f;
		}
	}
}
