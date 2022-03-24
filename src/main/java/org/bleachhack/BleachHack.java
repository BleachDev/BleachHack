/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack;

import com.google.gson.JsonObject;

import net.fabricmc.api.ModInitializer;

import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.Level;
import org.bleachhack.command.CommandManager;
import org.bleachhack.command.CommandSuggestor;
import org.bleachhack.eventbus.BleachEventBus;
import org.bleachhack.eventbus.handler.InexactEventHandler;
import org.bleachhack.gui.clickgui.ModuleClickGuiScreen;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.FriendManager;
import org.bleachhack.util.Watermark;
import org.bleachhack.util.io.BleachFileHelper;
import org.bleachhack.util.io.BleachFileMang;

public class BleachHack implements ModInitializer {

	private static BleachHack instance = null;

	public static final String VERSION = "5.2";
	public static final int INTVERSION = 39;
	public static Watermark watermark;

	public static BleachEventBus eventBus;

	public static FriendManager friendMang;

	private static CompletableFuture<JsonObject> updateJson;

	//private BleachFileMang bleachFileManager;

	public static BleachHack getInstance() {
		return instance;
	}

	public BleachHack() {
		if (instance != null) {
			throw new RuntimeException("A DarkHack instance already exists.");
		}
	}

	// Phase 1
	// TODO: base-rewrite
	@Override
	public void onInitialize() {
		long initStartTime = System.currentTimeMillis();

		instance = this;
		watermark = new Watermark();
		eventBus = new BleachEventBus(new InexactEventHandler("bleachhack"), BleachLogger.logger);

		friendMang = new FriendManager();

		//this.eventBus = new EventBus();
		//this.bleachFileManager = new BleachFileMang();

		BleachFileMang.init();

		BleachFileHelper.readOptions();
		BleachFileHelper.readFriends();

		BleachLogger.logger.log(Level.INFO, "Loaded DarkHack (Phase 1) in %d ms.", System.currentTimeMillis() - initStartTime);
	}

	// Phase 2
	// Called after most of the game has been initialized in MixinMinecraftClient so all game resources can be accessed
	public void postInit() {
		long initStartTime = System.currentTimeMillis();

		ModuleManager.loadModules(this.getClass().getClassLoader().getResourceAsStream("bleachhack.modules.json"));
		BleachFileHelper.readModules();

		// TODO: move ClickGui and UI to phase 1
		ModuleClickGuiScreen.INSTANCE.initWindows();
		BleachFileHelper.readClickGui();
		BleachFileHelper.readUI();

		CommandManager.loadCommands(this.getClass().getClassLoader().getResourceAsStream("bleachhack.commands.json"));
		CommandSuggestor.start();

		BleachFileHelper.startSavingExecutor();

		BleachLogger.logger.log(Level.INFO, "Loaded DarkHack (Phase 2) in %d ms.", System.currentTimeMillis() - initStartTime);
	}

	public static JsonObject getUpdateJson() {
		try {
			return updateJson.get();
		} catch (Exception e) {
			return null;
		}
	}
}
