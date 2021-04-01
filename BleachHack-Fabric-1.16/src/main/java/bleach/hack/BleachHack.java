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
package bleach.hack;

import com.google.common.eventbus.EventBus;
import com.google.gson.JsonElement;

import bleach.hack.command.CommandManager;
import bleach.hack.gui.title.BleachTitleScreen;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.util.FriendManager;
import bleach.hack.util.file.BleachFileHelper;
import bleach.hack.util.file.BleachFileMang;
import net.fabricmc.api.ModInitializer;

public class BleachHack implements ModInitializer {

	public static final String VERSION = "0.15.2";
	public static final int INTVERSION = 27;

	public static final EventBus eventBus = new EventBus();

	public static FriendManager friendMang;

	@Override
	public void onInitialize() {
		BleachFileMang.init();
		BleachFileHelper.readModules();

		ClickGui.clickGui.initWindows();
		BleachFileHelper.readClickGui();
		BleachFileHelper.readFriends();

		CommandManager.readPrefix();

		// v This makes a scat fetishist look like housekeeping.
		eventBus.register(new ModuleManager());
		// wait why do we need this ^?
		// Because I was too lazy to implement a proper keybind system and I left the
		// keypress handler in ModuleManager as a subscribed event. TODO: Proper Keybind
		// System

		JsonElement mainMenu = BleachFileHelper.readMiscSetting("customTitleScreen");
		if (mainMenu != null && !mainMenu.getAsBoolean()) {
			BleachTitleScreen.customTitleScreen = false;
		}
	}
}
