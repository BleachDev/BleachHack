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

import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import bleach.hack.BleachHack;
import bleach.hack.gui.clickgui.window.ClickGuiWindow;
import bleach.hack.gui.clickgui.window.UIWindow;
import bleach.hack.gui.clickgui.window.UIWindow.Position;
import bleach.hack.gui.window.Window;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.module.mods.UI;
import bleach.hack.setting.base.SettingBase;
import bleach.hack.util.BleachLogger;

public class BleachFileHelper {

	public static boolean SCHEDULE_SAVE_MODULES = false;
	public static boolean SCHEDULE_SAVE_FRIENDS = false;
	public static boolean SCHEDULE_SAVE_CLICKGUI = false;
	public static boolean SCHEDULE_SAVE_UI = false;

	public static void saveModules() {
		SCHEDULE_SAVE_MODULES = false;

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

		BleachJsonHelper.setJsonFile(jo, "modules.json");
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
							BleachLogger.errorMessage("Error enabling " + e.getKey() + ", Disabling!");
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
						HashMap<String, Integer> sNames = new HashMap<>();

						for (SettingBase sb : mod.getSettings()) {
							String name = sNames.containsKey(sb.getName()) ? sb.getName() + sNames.get(sb.getName()) : sb.getName();

							if (name.equals(se.getKey())) {
								sb.readSettings(se.getValue());
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

	public static void saveClickGui() {
		SCHEDULE_SAVE_CLICKGUI = false;

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

		BleachJsonHelper.setJsonFile(jo, "clickgui.json");
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
		SCHEDULE_SAVE_UI = false;

		JsonObject jo = new JsonObject();

		for (Entry<String, UIWindow> w : UI.uiScreen.uiWindows.entrySet()) {
			JsonObject jw = new JsonObject();
			jw.addProperty("x", w.getValue().position.xPercent);
			jw.addProperty("y", w.getValue().position.yPercent);

			JsonObject ja = new JsonObject();
			for (Pair<String, Integer> atm: w.getValue().position.getAttachments()) {
				ja.add(atm.getLeft(), new JsonPrimitive(atm.getRight()));
			}

			if (ja.size() > 0) {
				jw.add("attachments", ja);
			}
			
			jo.add(w.getKey(), jw);
		}

		BleachJsonHelper.setJsonFile(jo, "ui.json");
	}

	public static void readUI() {
		JsonObject jo = BleachJsonHelper.readJsonFile("ui.json");

		if (jo == null)
			return;

		Map<String, UIWindow> uiWindows = UI.uiScreen.uiWindows;
		for (Entry<String, JsonElement> e : jo.entrySet()) {
			if (!e.getValue().isJsonObject() || !uiWindows.containsKey(e.getKey()))
				continue;

			JsonObject jw = e.getValue().getAsJsonObject();
			if (!jw.has("x") || !jw.has("y"))
				continue;

			Position pos = new Position(jw.get("x").getAsDouble(), jw.get("y").getAsDouble());
			
			if (jw.has("attachments")) {
				for (Entry<String, JsonElement> ja : jw.get("attachments").getAsJsonObject().entrySet()) {
					pos.addAttachment(Pair.of(ja.getKey(), ja.getValue().getAsInt()));
				}
			}

			uiWindows.get(e.getKey()).position = pos;
		}
	}

	public static void readFriends() {
		BleachHack.friendMang.addAll(BleachFileMang.readFileLines("friends.txt"));
	}

	public static void saveFriends() {
		SCHEDULE_SAVE_FRIENDS = false;

		String toWrite = "";
		for (String s : BleachHack.friendMang.getFriends())
			toWrite += s + "\n";

		BleachFileMang.createEmptyFile("friends.txt");
		BleachFileMang.appendFile(toWrite, "friends.txt");
	}

	public static JsonElement readMiscSetting(String key) {
		JsonElement element = BleachJsonHelper.readJsonElement(key, "misc.json");

		try {
			return element;
		} catch (Exception e) {
			return null;
		}
	}

	public static void saveMiscSetting(String key, JsonElement value) {
		BleachJsonHelper.addJsonElement(key, value, "misc.json");
	}
}
