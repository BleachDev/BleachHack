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
package bleach.hack.utils.file;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import bleach.hack.BleachHack;
import bleach.hack.command.Command;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.window.Window;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.utils.FriendManager;

public class BleachFileHelper {
	
	public static boolean SCHEDULE_SAVE_MODULES = false;
	public static boolean SCHEDULE_SAVE_FRIENDS = false;
	public static boolean SCHEDULE_SAVE_CLICKGUI = false;

	public static void saveModules() {
		SCHEDULE_SAVE_MODULES = false;
		
		JsonObject jo = new JsonObject();
		
		System.out.println("Saving Modules!");
		for (Module m: ModuleManager.getModules()) {
			JsonObject mo = new JsonObject();
			
			System.out.println("Saving: " + m.getName());
			if (m.isToggled() && !m.getName().equals("ClickGui") && !m.getName().equals("Freecam")) {
				mo.add("toggled", new JsonPrimitive(true));
				System.out.println(".. Module Is Enabled");
			} else if (m.isToggled()) {
				System.out.println(".. Module Is Enabled [SKIPPED]");
			}
			
			if (m.getKey() >= 0) {
				mo.add("bind", new JsonPrimitive(m.getKey()));
				System.out.println(".. Saving Bind: KEY" + m.getKey());
			}
			
			if (!m.getSettings().isEmpty()) {
				System.out.println(".. Saving Settings");
				JsonObject so = new JsonObject();
				
				for (SettingBase s: m.getSettings()) {
					String name = s.getName();
					
					int extra = 0;
					while (so.has(name)) {
						extra++;
						name = s.getName() + extra;
					}
					
					so.add(name, s.saveSettings());
					System.out.println("... Saving " + name);
				}
				
				mo.add("settings", so);
			}
			
			if (mo.size() != 0) {
				System.out.println(".. Adding " + mo.size() + " Elements To Config");
				jo.add(m.getName(), mo);
			} else {
				System.out.println(".. Module is empty! Not saving");
			}
		}
		
		System.out.println("Writing!");
		BleachJsonHelper.setJsonFile(jo, "modules.json");
		System.out.println("Done Writing!");
	}

	public static void readModules() {
		JsonObject jo = BleachJsonHelper.readJsonFile("modules.json");
		
		if (jo == null) {
			System.out.println("Null json found! Returning");
			return;
		}
		
		System.out.println("Module config found:\n" + new GsonBuilder().setPrettyPrinting().create().toJson(jo));
		for (Entry<String, JsonElement> e: jo.entrySet()) {
			Module mod = ModuleManager.getModuleByName(e.getKey());
			System.out.println("Initing Mod: " + e.getKey());
			
			if (mod == null) {
				System.out.println(".. Null module found! Continuing");
				continue;
			}
			
			if (e.getValue().isJsonObject()) {
				JsonObject mo = e.getValue().getAsJsonObject();
				if (mo.has("toggled")) {
					System.out.println(".. Enabling");
					mod.setToggled(true);
				}
				
				if (mo.has("bind") && mo.get("bind").isJsonPrimitive() && mo.get("bind").getAsJsonPrimitive().isNumber()) {
					System.out.println(".. Binding To: KEY" + mo.get("bind").getAsInt());
					mod.setKey(mo.get("bind").getAsInt());
				}
				
				if (mo.has("settings") && mo.get("settings").isJsonObject()) {
					System.out.println(".. Setting Settings");
					
					// Map to keep track if there are multiple settings with the same name
					HashMap<String, Integer> sNames = new HashMap<>();
					
					for (Entry<String, JsonElement> se: mo.get("settings").getAsJsonObject().entrySet()) {
						for (SettingBase sb: mod.getSettings()) {
							String name = sNames.containsKey(sb.getName()) ? sb.getName() + sNames.get(sb.getName()) : sb.getName();
							
							if (name.equals(se.getKey())) {
								System.out.println(".... Reading Setting: \"" + name + "\"");
								sb.readSettings(se.getValue());
								sNames.put(sb.getName(), sNames.containsKey(sb.getName()) ? sNames.get(sb.getName()) + 1 : 1);
								break;
							}
						}
					}
					
					System.out.println(".. Done Initing Mod: " + mod.getName() + " / Toggled: " + mod.isToggled() + " / Bind: " + mod.getKey());
				}
			} else {
				System.out.println(".. Config is not a JsonObject! Its a " + e.getValue().getClass().getSimpleName());
			}
		}
	}

	public static void saveClickGui() {
		SCHEDULE_SAVE_CLICKGUI = false;
		
		BleachFileMang.createEmptyFile("clickgui.txt");

		String text = "";
		for (Window w: ClickGui.clickGui.windows) text += w.x1 + ":" + w.y1 + "\n";

		BleachFileMang.appendFile(text, "clickgui.txt");
	}

	public static void readClickGui() {
		List<String> lines = BleachFileMang.readFileLines("clickgui.txt");

		try {
			int c = 0;
			for (Window w: ClickGui.clickGui.windows) {
				w.x1 = Integer.parseInt(lines.get(c).split(":")[0]);
				w.y1 = Integer.parseInt(lines.get(c).split(":")[1]);
				c++;
			}
		} catch (Exception e) {}
	}

	public static void readPrefix() {
		try{ Command.PREFIX = BleachFileMang.readFileLines("prefix.txt").get(0); } catch (Exception e) {}
	}

	public static void readFriends() {
		BleachHack.friendMang = new FriendManager(BleachFileMang.readFileLines("friends.txt"));
	}

	public static void saveFriends() {
		SCHEDULE_SAVE_FRIENDS = false;
		
		String toWrite = "";
		for (String s: BleachHack.friendMang.getFriends()) toWrite += s + "\n";

		BleachFileMang.createEmptyFile("friends.txt");
		BleachFileMang.appendFile(toWrite, "friends.txt");
	}

	public static String readMiscSetting(String key) {
		JsonElement element = BleachJsonHelper.readJsonElement(key, "misc.json");

		try {
			return element.getAsString();
		} catch (Exception e) {
			return null;
		}
	}

	public static void saveMiscSetting(String key, String value) {
		BleachJsonHelper.addJsonElement(key, new JsonPrimitive(value), "misc.json");
	}

}
