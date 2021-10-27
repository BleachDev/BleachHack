/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util.io;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import bleach.hack.BleachHack;
import bleach.hack.gui.clickgui.window.ClickGuiWindow;
import bleach.hack.gui.clickgui.window.UIWindow;
import bleach.hack.gui.clickgui.window.UIWindow.Position;
import bleach.hack.gui.option.Option;
import bleach.hack.gui.window.Window;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.module.mods.UI;
import bleach.hack.module.setting.base.SettingBase;
import bleach.hack.util.BleachLogger;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

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
		JsonObject jo = new JsonObject();

		for (Module m : ModuleManager.getModules()) {
			JsonObject mo = new JsonObject();

			if (m.isDefaultEnabled() != m.isEnabled() && !m.getName().equals("ClickGui") && !m.getName().equals("Freecam")) {
				mo.add("toggled", new JsonPrimitive(m.isEnabled()));
			}

			if (m.getKey() >= 0 || m.getDefaultKey() >= 0 /* Force saving of modules with a default bind to prevent them reapplying the
			 * default bind */) {
				mo.add("bind", new JsonPrimitive(m.getKey()));
			}

			if (!m.getSettings().isEmpty()) {
				JsonObject so = new JsonObject();
				// Seperate JsonObject with all the settings to keep the extra number so when it
				// reads, it doesn't mess up the order
				JsonObject fullSo = new JsonObject();

				for (SettingBase s : m.getSettings()) {
					String name = s.getName();

					int extra = 0;
					while (fullSo.has(name)) {
						extra++;
						name = s.getName() + extra;
					}

					fullSo.add(name, s.saveSettings());
					if (!s.isDefault())
						so.add(name, s.saveSettings());
				}

				if (so.size() != 0) {
					mo.add("settings", so);
				}

			}

			if (mo.size() != 0) {
				jo.add(m.getName(), mo);
			}
		}

		BleachJsonHelper.setJsonFile("modules.json", jo);
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
						try {
							BleachLogger.error("Error enabling " + e.getKey() + ", Disabling!");
							mod.setEnabled(false);
						} catch (Exception ex2) {
							// ????
						}
					}
				}

				if (mo.has("bind") && mo.get("bind").isJsonPrimitive() && mo.get("bind").getAsJsonPrimitive().isNumber()) {
					mod.setKey(mo.get("bind").getAsInt());
				}

				if (mo.has("settings") && mo.get("settings").isJsonObject()) {
					for (Entry<String, JsonElement> se : mo.get("settings").getAsJsonObject().entrySet()) {
						// Map to keep track if there are multiple settings with the same name
						Map<String, Integer> sNames = new HashMap<>();

						for (SettingBase sb : mod.getSettings()) {
							String name = sNames.containsKey(sb.getName()) ? sb.getName() + sNames.get(sb.getName()) : sb.getName();

							if (name.equals(se.getKey())) {
								try {
									sb.readSettings(se.getValue());
								} catch (Exception ignored) {}

								break;
							} else {
								sNames.put(sb.getName(), sNames.containsKey(sb.getName()) ? sNames.get(sb.getName()) + 1 : 1);
							}
						}
					}
				}
			}
		}
	}

	public static void saveOptions() {
		JsonObject jo = new JsonObject();

		for (Option<?> o: Option.OPTIONS) {
			jo.add(o.getName(), o.serialize());
		}

		BleachJsonHelper.setJsonFile("options.json", jo);
	}

	public static void readOptions() {
		JsonObject jo = BleachJsonHelper.readJsonFile("options.json");

		if (jo == null)
			return;

		for (Option<?> o: Option.OPTIONS) {
			if (jo.has(o.getName())) {
				o.deserialize(jo.get(o.getName()));
			}
		}
	}

	public static void saveClickGui() {
		JsonObject jo = new JsonObject();

		for (Window w : ClickGui.clickGui.getWindows()) {
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

			for (Window w : ClickGui.clickGui.getWindows()) {
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

		for (Entry<String, UIWindow> w : UI.uiContainer.windows.entrySet()) {
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

		Map<String, UIWindow> uiWindows = UI.uiContainer.windows;
		for (Entry<String, JsonElement> e : jo.entrySet()) {
			if (!e.getValue().isJsonObject() || !uiWindows.containsKey(e.getKey()))
				continue;

			JsonObject jw = e.getValue().getAsJsonObject();
			if (!jw.has("x") || !jw.has("y"))
				continue;

			Position pos = new Position(jw.get("x").getAsDouble(), jw.get("y").getAsDouble());

			if (jw.has("attachments")) {
				for (Entry<String, JsonElement> ja : jw.get("attachments").getAsJsonObject().entrySet()) {
					if (uiWindows.keySet().contains(ja.getKey()) || ja.getKey().length() == 1) {
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
