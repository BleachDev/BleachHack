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
import com.google.gson.JsonObject;

import bleach.hack.command.CommandManager;
import bleach.hack.command.CommandSuggestor;
import bleach.hack.eventbus.BleachEventBus;
import bleach.hack.gui.BleachTitleScreen;
import bleach.hack.gui.option.Option;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.BleachPlayerManager;
import bleach.hack.util.FriendManager;
import bleach.hack.util.Watermark;
import bleach.hack.util.io.BleachFileHelper;
import bleach.hack.util.io.BleachFileMang;
import bleach.hack.util.io.BleachJsonHelper;
import bleach.hack.util.io.BleachOnlineMang;
import net.fabricmc.api.ModInitializer;
import net.minecraft.SharedConstants;

import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.Level;

public class BleachHack implements ModInitializer {

	private static BleachHack instance = null;

	public static final String VERSION = "1.2.2";
	public static final int INTVERSION = 36;
	public static Watermark watermark;

	public static BleachEventBus eventBus;

	public static FriendManager friendMang;
	public static BleachPlayerManager playerMang;

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

	//TODO: base-rewrite
	@Override
	public void onInitialize() {
		long initStartTime = System.currentTimeMillis();

		instance = this;
		watermark = new Watermark();
		eventBus = new BleachEventBus(BleachLogger.logger);

		friendMang = new FriendManager();
		playerMang = new BleachPlayerManager();

		//this.eventBus = new EventBus();
		//this.bleachFileManager = new BleachFileMang();

		BleachFileMang.init();
		BleachFileHelper.startSavingExecutor();

		BleachFileHelper.readOptions();
		BleachFileHelper.readFriends();

		if (Option.PLAYERLIST_SHOW_AS_BH_USER.getValue()) {
			playerMang.startPinger();
		}

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

	// Called after most of the game has been initialized in MixinMinecraftClient so all game resources can be accessed
	public void postInit() {
		long initStartTime = System.currentTimeMillis();

		ModuleManager.loadModules(this.getClass().getClassLoader().getResourceAsStream("bleachhack.modules.json"));
		BleachFileHelper.readModules();

		// TODO: move ClickGui and UI to phase 1
		ClickGui.clickGui.initWindows();
		BleachFileHelper.readClickGui();
		BleachFileHelper.readUI();

		CommandManager.loadCommands(this.getClass().getClassLoader().getResourceAsStream("bleachhack.commands.json"));
		CommandSuggestor.start();

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
