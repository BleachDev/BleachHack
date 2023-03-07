/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.fabricmc.api.ModInitializer;
import net.minecraft.SharedConstants;

import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.Level;
import org.bleachhack.command.CommandManager;
import org.bleachhack.command.CommandSuggestor;
import org.bleachhack.eventbus.BleachEventBus;
import org.bleachhack.eventbus.handler.InexactEventHandler;
import org.bleachhack.gui.BleachTitleScreen;
import org.bleachhack.gui.clickgui.ModuleClickGuiScreen;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.setting.option.Option;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.FriendManager;
import org.bleachhack.util.Watermark;
import org.bleachhack.util.io.BleachFileHelper;
import org.bleachhack.util.io.BleachFileMang;
import org.bleachhack.util.io.BleachJsonHelper;
import org.bleachhack.util.io.BleachOnlineMang;

public class BleachHack implements ModInitializer {

	private static BleachHack instance = null;

	public static final String VERSION = "1.2.6";
	public static final int INTVERSION = 40;
	public static Watermark watermark = new Watermark();

	public static BleachEventBus eventBus = new BleachEventBus(new InexactEventHandler("bleachhack"), BleachLogger.logger);

	public static FriendManager friendMang;

	private static CompletableFuture<JsonObject> updateJson;

	//private BleachFileMang bleachFileManager;

	public static BleachHack getInstance() {
		return instance;
	}

	public BleachHack() {
		if (instance != null) {
			throw new RuntimeException("A BleachHack instance already exists.");
		}
	}

	// Phase 1
	// TODO: base-rewrite
	@Override
	public void onInitialize() {
		long initStartTime = System.currentTimeMillis();

		instance = this;

		friendMang = new FriendManager();

		//this.eventBus = new EventBus();
		//this.bleachFileManager = new BleachFileMang();

		BleachFileMang.init();

		BleachFileHelper.readOptions();
		BleachFileHelper.readFriends();

		if (Option.GENERAL_CHECK_FOR_UPDATES.getValue()) {
			updateJson = BleachOnlineMang.getResourceAsync("update/" + SharedConstants.getGameVersion().getName().replace(' ', '_') + ".json", BodyHandlers.ofString())
					.thenApply(s -> BleachJsonHelper.parseOrNull(s, JsonObject.class));
		}

		JsonElement mainMenu = BleachFileHelper.readMiscSetting("customTitleScreen");
		if (mainMenu != null && !mainMenu.getAsBoolean()) {
			BleachTitleScreen.customTitleScreen = false;
		}

		BleachLogger.logger.log(Level.INFO, "Loaded BleachHack (Phase 1) in %d ms.", System.currentTimeMillis() - initStartTime);
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

		BleachLogger.logger.log(Level.INFO, "Loaded BleachHack (Phase 2) in %d ms.", System.currentTimeMillis() - initStartTime);
	}

	public static JsonObject getUpdateJson() {
		try {
			return updateJson.get();
		} catch (Exception e) {
			return null;
		}
	}
}
