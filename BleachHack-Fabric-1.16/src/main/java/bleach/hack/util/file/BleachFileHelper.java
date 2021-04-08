/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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
package bleach.hack.util.file;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import bleach.hack.BleachHack;
import bleach.hack.gui.window.Window;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.setting.base.SettingBase;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.FriendManager;

public class BleachFileHelper {

	public static boolean SCHEDULE_SAVE_MODULES = false;
	public static boolean SCHEDULE_SAVE_FRIENDS = false;
	public static boolean SCHEDULE_SAVE_CLICKGUI = false;

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

		BleachFileMang.createEmptyFile("clickgui.txt");

		String text = "";
		for (Window w : ClickGui.clickGui.getWindows())
			text += w.x1 + ":" + w.y1 + "\n";

		BleachFileMang.appendFile(text, "clickgui.txt");
	}

	public static void readClickGui() {
		List<String> lines = BleachFileMang.readFileLines("clickgui.txt");

		try {
			int c = 0;
			for (Window w : ClickGui.clickGui.getWindows()) {
				w.x1 = Integer.parseInt(lines.get(c).split(":")[0]);
				w.y1 = Integer.parseInt(lines.get(c).split(":")[1]);
				c++;
			}
		} catch (Exception e) {
		}
	}

	public static void readFriends() {
		BleachHack.friendMang = new FriendManager(BleachFileMang.readFileLines("friends.txt"));
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
