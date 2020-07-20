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

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import bleach.hack.BleachHack;
import bleach.hack.command.Command;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.gui.window.Window;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.utils.FriendManager;

public class BleachFileHelper {

	private static Gson jsonWriter = new GsonBuilder().setPrettyPrinting().create();

	public static void saveModules() {
		BleachFileMang.createEmptyFile("modules.txt");

		String lines = "";
		for (Module m: ModuleManager.getModules()) {
			if (m.getName() == "ClickGui" || m.getName() == "Freecam") continue;
			lines += m.getName() + ":" + m.isToggled() + "\n";
		}

		BleachFileMang.appendFile(lines, "modules.txt");
	}

	public static void readModules() {
		List<String> lines = BleachFileMang.readFileLines("modules.txt");

		for (Module m: ModuleManager.getModules()) {
			for (String s: lines) {
				String[] line = s.split(":");
				try {
					if (line[0].contains(m.getName()) && line[1].contains("true")) {
						m.toggle();
						break;
					}
				} catch (Exception e) {}
			}
		}
	}

	public static void saveModSettings() {
		BleachFileMang.createEmptyFile("settings.txt");

		String lines = "";
		for (Module m: ModuleManager.getModules()) {
			String line = m.getName();
			int count = 0;

			for (SettingBase set: m.getSettings()) {
				if (set instanceof SettingSlider) line += ":" + m.getSettings().get(count).asSlider().getValue();
				if (set instanceof SettingMode) line += ":" + m.getSettings().get(count).asMode().mode;
				if (set instanceof SettingToggle) line += ":" + m.getSettings().get(count).asToggle().state;
				count++;
			}
			lines += line + "\n";
		}

		BleachFileMang.appendFile(lines, "settings.txt");
	}

	public static void readSettings() {
		List<String> lines = BleachFileMang.readFileLines("settings.txt");

		for (Module m: ModuleManager.getModules()) {
			for (String s: lines) {
				String[] line = s.split(":");
				if (!line[0].startsWith(m.getName())) continue;
				int count = 0;

				for (SettingBase set: m.getSettings()) {
					try {
						if (set instanceof SettingSlider) {
							m.getSettings().get(count).asSlider().setValue(Double.parseDouble(line[count+1]));}
						if (set instanceof SettingMode) {
							m.getSettings().get(count).asMode().mode = Integer.parseInt(line[count+1]);}
						if (set instanceof SettingToggle) {
							m.getSettings().get(count).asToggle().state = Boolean.parseBoolean(line[count+1]);}
					} catch (Exception e) {}
					count++;
				}
			}
		}
	}

	public static void saveBinds() {
		BleachFileMang.createEmptyFile("binds.txt");

		String lines = "";
		for (Module m: ModuleManager.getModules()) {
			lines += m.getName() + ":" + m.getKey() + "\n";
		}

		BleachFileMang.appendFile(lines, "binds.txt");
	}

	public static void readBinds() {
		List<String> lines = BleachFileMang.readFileLines("binds.txt");

		for (Module m: ModuleManager.getModules()) {
			for (String s: lines) {
				String[] line = s.split(":");
				if (!line[0].startsWith(m.getName())) continue;
				try { m.setKey(Integer.parseInt(line[line.length - 1])); } catch (Exception e) {}
			}
		}
	}

	public static void saveClickGui() {
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
		String toWrite = "";
		for (String s: BleachHack.friendMang.getFriends()) toWrite += s + "\n";

		BleachFileMang.createEmptyFile("friends.txt");
		BleachFileMang.appendFile(toWrite, "friends.txt");
	}

	public static String readMiscSetting(String key) {
		List<String> lines = BleachFileMang.readFileLines("misc.json");

		if (lines.isEmpty()) return null;

		String merged = String.join("\n", lines);

		String value = null;

		try {
			JsonElement mainJE = new JsonParser().parse(merged);

			if (mainJE.isJsonObject()) {
				JsonObject mainJO = mainJE.getAsJsonObject();

				value = mainJO.get(key).getAsString();
			}
		} catch (JsonParseException e) {
			System.err.println("Json error Trying to read misc settings! DELETING ENTIRE FILE!");
			e.printStackTrace();

			BleachFileMang.deleteFile("misc.json");
		}

		return value;
	}

	public static void saveMiscSetting(String key, String value) {
		JsonObject file = null;
		boolean overwrite = false;

		if (!BleachFileMang.fileExists("misc.json")) {
			overwrite = true;
		} else {
			List<String> lines = BleachFileMang.readFileLines("misc.json");

			if (lines.isEmpty()) {
				overwrite = true;
			} else {
				String merged = String.join("\n", lines);

				try {
					file = new JsonParser().parse(merged).getAsJsonObject();
				} catch (Exception e) {
					e.printStackTrace();
					overwrite = true;
				}
			}
		}

		BleachFileMang.createEmptyFile("misc.json");
		if (overwrite) {
			JsonObject mainJO = new JsonObject();
			mainJO.add(key, new JsonPrimitive(value));

			BleachFileMang.appendFile(jsonWriter.toJson(mainJO), "misc.json");
		} else {
			file.add(key, new JsonPrimitive(value));

			BleachFileMang.appendFile(jsonWriter.toJson(file), "misc.json");
		}
	}
}
