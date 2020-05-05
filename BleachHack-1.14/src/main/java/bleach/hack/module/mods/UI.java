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

import bleach.hack.gui.IngameOverlay;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class UI extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(true, "Arraylist"),
			new SettingToggle(false, "FPS"),
			new SettingToggle(false, "Ping"),
			new SettingToggle(false, "Coords"),
			new SettingToggle(false, "Server"));
	
	private IngameOverlay gui = new IngameOverlay();
	
	public UI() {
		super("UI", -1, Category.RENDER, "Shows stuff onscreen.", settings);
	}
	
	public void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	public void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	@SubscribeEvent
	public void drawOverlay(RenderGameOverlayEvent.Text event) {
		gui.bottomLeftList.clear();
		if (getSettings().get(0).toToggle().state) gui.drawArrayList();
		if (getSettings().get(3).toToggle().state) {
			gui.addNetherCoords();
			gui.addCoords();
		}
		if (getSettings().get(1).toToggle().state) gui.addFPS();
		try{ if (getSettings().get(2).toToggle().state) gui.addPing(); } catch (Exception e) {}
		try{ if (getSettings().get(4).toToggle().state) gui.drawServerInfo(); } catch (Exception e) {}
		gui.drawBottomLeft();
	}

}
