/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack;

import com.google.gson.JsonElement;

import bleach.hack.command.CommandManager;
import bleach.hack.command.CommandSuggestor;
import bleach.hack.eventbus.BleachEventBus;
import bleach.hack.gui.title.BleachTitleScreen;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.FriendManager;
import bleach.hack.util.io.BleachFileHelper;
import bleach.hack.util.io.BleachFileMang;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;

public class BleachHack implements ModInitializer {

	private static BleachHack instance = null;

	public static final String VERSION = "1.1";
	public static final int INTVERSION = 33;

	public static BleachEventBus eventBus;

	public static FriendManager friendMang;

	//private BleachFileMang bleachFileManager;

	public static BleachHack getInstance() {
		return instance;
	}

	public BleachHack() {
		if (instance != null) {
			throw new RuntimeException("A BleachHack instance already exists.");
		}
	}

	@Override
	public void onInitialize() {
		long initStartTime = System.currentTimeMillis();

		if (instance != null) {
			throw new RuntimeException("BleachHack has already been initialized.");
		}

		instance = this;
		eventBus = new BleachEventBus(BleachLogger.logger);

		//TODO base-rewrite
		//this.eventBus = new EventBus();
		//this.bleachFileManager = new BleachFileMang();
		BleachFileMang.init();
		ModuleManager.loadModules(this.getClass().getClassLoader().getResourceAsStream("bleachhack.modules.json"));
		BleachFileHelper.readModules();

		ClickGui.clickGui.initWindows();
		BleachFileHelper.readClickGui();
		BleachFileHelper.readFriends();
		BleachFileHelper.readUI();

		CommandManager.readPrefix();
		CommandManager.loadCommands(this.getClass().getClassLoader().getResourceAsStream("bleachhack.commands.json"));
		CommandSuggestor.start();

		JsonElement mainMenu = BleachFileHelper.readMiscSetting("customTitleScreen");
		if (mainMenu != null && !mainMenu.getAsBoolean()) {
			BleachTitleScreen.customTitleScreen = false;
		}

		BleachLogger.logger.log(Level.INFO, "Loaded BleachHack in %d ms.", System.currentTimeMillis() - initStartTime);
	}
}
