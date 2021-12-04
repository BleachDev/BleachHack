/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.io;

import java.util.List;

import org.bleachhack.util.BleachLogger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class BleachJsonHelper {

	private static final Gson jsonWriter = new GsonBuilder().setPrettyPrinting().create();

	public static void addJsonElement(String path, String key, JsonElement element) {
		JsonObject file = null;
		boolean overwrite = false;

		if (!BleachFileMang.fileExists(path)) {
			overwrite = true;
		} else {
			List<String> lines = BleachFileMang.readFileLines(path);

			if (lines.isEmpty()) {
				overwrite = true;
			} else {
				String merged = String.join("\n", lines);

				try {
					file = new JsonParser().parse(merged).getAsJsonObject();
				} catch (Exception e) {
					BleachLogger.logger.error("Error trying to read json file \"" + path + "\", Overwriting file to add element!", e);
					overwrite = true;
				}
			}
		}

		BleachFileMang.createEmptyFile(path);
		if (overwrite) {
			JsonObject mainJO = new JsonObject();
			mainJO.add(key, element);

			BleachFileMang.appendFile(path, jsonWriter.toJson(mainJO));
		} else {
			file.add(key, element);

			BleachFileMang.appendFile(path, jsonWriter.toJson(file));
		}
	}

	public static void setJsonFile(String path, JsonObject element) {
		BleachFileMang.createEmptyFile(path);
		BleachFileMang.appendFile(path, jsonWriter.toJson(element));
	}

	public static JsonElement readJsonElement(String path, String key) {
		JsonObject jo = readJsonFile(path);

		if (jo == null)
			return null;

		if (jo.has(key)) {
			return jo.get(key);
		}

		return null;
	}

	public static JsonObject readJsonFile(String path) {
		List<String> lines = BleachFileMang.readFileLines(path);

		if (lines.isEmpty())
			return null;

		String merged = String.join("\n", lines);

		try {
			return new JsonParser().parse(merged).getAsJsonObject();
		} catch (JsonParseException | IllegalStateException e) {
			BleachLogger.logger.error("Error trying to read json file \"" + path + "\", Deleting file!", e);

			BleachFileMang.deleteFile(path);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends JsonElement> T parseOrNull(String json, Class<T> type) {
		try {
			return (T) new JsonParser().parse(json);
		} catch (Exception e) {
			return null;
		}
	}

	public static String formatJson(String json) {
		return jsonWriter.toJson(new JsonParser().parse(json));
	}

	public static String formatJson(JsonElement json) {
		return jsonWriter.toJson(json);
	}
}
