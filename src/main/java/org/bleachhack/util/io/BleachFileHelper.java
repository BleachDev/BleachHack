/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.io;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.bleachhack.BleachHack;
import org.bleachhack.gui.clickgui.ModuleClickGuiScreen;
import org.bleachhack.gui.clickgui.UIClickGuiScreen;
import org.bleachhack.gui.clickgui.window.ClickGuiWindow;
import org.bleachhack.gui.clickgui.window.UIWindow;
import org.bleachhack.gui.clickgui.window.UIWindow.Position;
import org.bleachhack.gui.window.Window;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.setting.module.ModuleSetting;
import org.bleachhack.setting.option.Option;
import org.bleachhack.util.BleachLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BleachFileHelper {

	private static ScheduledExecutorService savingExecutor;

	public static AtomicBoolean SCHEDULE_SAVE_MODULES = new AtomicBoolean();
	public static AtomicBoolean SCHEDULE_SAVE_OPTIONS = new AtomicBoolean();
	public static AtomicBoolean SCHEDULE_SAVE_FRIENDS = new AtomicBoolean();
	public static AtomicBoolean SCHEDULE_SAVE_CLICKGUI = new AtomicBoolean();
	public static AtomicBoolean SCHEDULE_SAVE_UI = new AtomicBoolean();

	public static void startSavingExecutor() {
		if (savingExecutor == null)
			savingExecutor = MoreExecutors.getExitingScheduledExecutorService(new ScheduledThreadPoolExecutor(1));

		savingExecutor.scheduleAtFixedRate(() -> {
			if (SCHEDULE_SAVE_MODULES.getAndSet(false)) saveModules();
			if (SCHEDULE_SAVE_OPTIONS.getAndSet(false)) saveOptions();
			if (SCHEDULE_SAVE_CLICKGUI.getAndSet(false)) saveClickGui();
			if (SCHEDULE_SAVE_FRIENDS.getAndSet(false)) saveFriends();
			if (SCHEDULE_SAVE_UI.getAndSet(false)) saveUI();
		}, 0, 5, TimeUnit.SECONDS);
	}

	public static void stopSavingExecutor() {
		savingExecutor.shutdown();
		savingExecutor = null;
	}

	public static void saveModules() {
		JsonObject json = new JsonObject();

		for (Module mod : ModuleManager.getModules()) {
			JsonObject modjson = new JsonObject();

			if (mod.isEnabled() != mod.isDefaultEnabled() && !mod.getName().equals("ClickGui") && !mod.getName().equals("Freecam")) {
				modjson.add("toggled", new JsonPrimitive(mod.isEnabled()));
			}

			JsonObject setjson = new JsonObject();
			Map<String, ModuleSetting<?>> settingMap = getSettingMap(mod.getSettings());
			for (Entry<String, ModuleSetting<?>> s: settingMap.entrySet()) {
				if (!s.getValue().isDefault())
					setjson.add(s.getKey(), s.getValue().write());
			}

			if (setjson.size() != 0)
				modjson.add("settings", setjson);

			if (modjson.size() != 0)
				json.add(mod.getName(), modjson);
		}

		BleachJsonHelper.setJsonFile("modules.json", json);
	}

	public static void readModules() {
		JsonObject jo = BleachJsonHelper.readJsonFile("modules.json");

		if (jo == null)
			return;

		for (Entry<String, JsonElement> e : jo.entrySet()) {
			Module mod = ModuleManager.getModule(e.getKey());

			if (mod == null)
				continue;

			if (e.getValue().isJsonObject()) {
				JsonObject mo = e.getValue().getAsJsonObject();
				if (mo.has("toggled")) {
					try {
						mod.setEnabled(mo.get("toggled").getAsBoolean());
					} catch (Exception ex) {
						BleachLogger.error("Error enabling " + e.getKey() + ", Disabling!");

						try {
							mod.setEnabled(false);
						} catch (Exception ex2) {
							// ????
						}
					}
				}

				if (mo.has("settings") && mo.get("settings").isJsonObject()) {
					Map<String, ModuleSetting<?>> settingMap = getSettingMap(mod.getSettings());

					for (Entry<String, JsonElement> se : mo.get("settings").getAsJsonObject().entrySet()) {
						try {
							 ModuleSetting<?> s = settingMap.get(se.getKey());
							 if (s != null) {
								 s.read(se.getValue());
							 } else {
								 BleachLogger.logger.warn("Error reading setting \"" + se.getKey() + "\" in module " + mod.getName() + ", removed?");
							 }
						} catch (Exception ex) {
							BleachLogger.logger.error("Error reading setting \"" + se.getKey() + "\" in module " + mod.getName() + ": " + se.getValue(), ex);
						}
					}
				}
			}
		}
	}

	private static Map<String, ModuleSetting<?>> getSettingMap(Collection<ModuleSetting<?>> settings) {
		Map<String, ModuleSetting<?>> settingMap = new HashMap<>();
		for (ModuleSetting<?> s: settings) {
			String name = s.getName();
			int i = 1;
			while (settingMap.containsKey(name))
				name = s.getName() + "$" + i++;

			settingMap.put(name, s);
		}

		return settingMap;
	}

	public static void saveOptions() {
		JsonObject jo = new JsonObject();

		for (Option<?> o: Option.OPTIONS) {
			jo.add(o.getName(), o.write());
		}

		BleachJsonHelper.setJsonFile("options.json", jo);
	}

	public static void readOptions() {
		JsonObject jo = BleachJsonHelper.readJsonFile("options.json");

		if (jo == null)
			return;

		for (Option<?> o: Option.OPTIONS) {
			JsonElement je = jo.get(o.getName());
			if (je != null)
				o.read(je);
		}
	}

	public static void saveClickGui() {
		JsonObject jo = new JsonObject();

		for (Window w : ModuleClickGuiScreen.INSTANCE.getWindows()) {
			JsonObject jw = new JsonObject();
			jw.addProperty("x", w.x1);
			jw.addProperty("y", w.y1);

			if (w instanceof ClickGuiWindow) {
				jw.addProperty("hidden", ((ClickGuiWindow) w).hiding);
			}

			jo.add(w.title, jw);
		}

		BleachJsonHelper.setJsonFile("clickgui.json", jo);
	}

	public static void readClickGui() {
		JsonObject jo = BleachJsonHelper.readJsonFile("clickgui.json");

		if (jo == null)
			return;

		for (Entry<String, JsonElement> e : jo.entrySet()) {
			if (!e.getValue().isJsonObject())
				continue;

			for (Window w : ModuleClickGuiScreen.INSTANCE.getWindows()) {
				if (w.title.equals(e.getKey())) {
					JsonObject jw = e.getValue().getAsJsonObject();

					try {
						w.x1 = jw.get("x").getAsInt();
						w.y1 = jw.get("y").getAsInt();

						if (w instanceof ClickGuiWindow && jw.has("hidden")) {
							((ClickGuiWindow) w).hiding = jw.get("hidden").getAsBoolean();
						}
					} catch (Exception ex) {
						BleachLogger.logger.error("Error trying to load clickgui window: " + e.getKey() + " with data: " + e.getValue());
					}
				}
			}
		}
	}

	public static void saveUI() {
		JsonObject jo = new JsonObject();

		for (Entry<String, UIWindow> w : UIClickGuiScreen.INSTANCE.getUIContainer().windows.entrySet()) {
			JsonObject jw = new JsonObject();
			jw.addProperty("x", w.getValue().position.xPercent);
			jw.addProperty("y", w.getValue().position.yPercent);

			JsonObject ja = new JsonObject();
			for (Object2IntMap.Entry<String> atm: w.getValue().position.getAttachments().object2IntEntrySet()) {
				ja.add(atm.getKey(), new JsonPrimitive(atm.getIntValue()));
			}

			if (ja.size() > 0) {
				jw.add("attachments", ja);
			}

			jo.add(w.getKey(), jw);
		}

		BleachJsonHelper.setJsonFile("ui.json", jo);
	}

	public static void readUI() {
		JsonObject jo = BleachJsonHelper.readJsonFile("ui.json");

		if (jo == null)
			return;

		Map<String, UIWindow> uiWindows = UIClickGuiScreen.INSTANCE.getUIContainer().windows;
		for (Entry<String, JsonElement> e : jo.entrySet()) {
			if (!e.getValue().isJsonObject() || !uiWindows.containsKey(e.getKey()))
				continue;

			JsonObject jw = e.getValue().getAsJsonObject();
			if (!jw.has("x") || !jw.has("y"))
				continue;

			Position pos = new Position(jw.get("x").getAsDouble(), jw.get("y").getAsDouble());

			if (jw.has("attachments")) {
				for (Entry<String, JsonElement> ja : jw.get("attachments").getAsJsonObject().entrySet()) {
					if (uiWindows.containsKey(ja.getKey()) || ja.getKey().length() == 1) {
						pos.addAttachment(ja.getKey(), ja.getValue().getAsInt());
					}
				}
			}

			uiWindows.get(e.getKey()).position = pos;
		}
	}

	public static void readFriends() {
		BleachHack.friendMang.addAll(BleachFileMang.readFileLines("friends.txt"));
	}

	public static void saveFriends() {
		String toWrite = "";
		for (String s : BleachHack.friendMang.getFriends())
			toWrite += s + "\n";

		BleachFileMang.createEmptyFile("friends.txt");
		BleachFileMang.appendFile("friends.txt", toWrite);
	}

	public static JsonElement readMiscSetting(String key) {
		JsonElement element = BleachJsonHelper.readJsonElement("misc.json", key);

		try {
			return element;
		} catch (Exception e) {
			return null;
		}
	}

	public static void saveMiscSetting(String key, JsonElement value) {
		BleachJsonHelper.addJsonElement("misc.json", key, value);
	}
}
