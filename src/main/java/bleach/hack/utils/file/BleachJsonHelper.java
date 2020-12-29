package bleach.hack.utils.file;

import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class BleachJsonHelper {

	private static Gson jsonWriter = new GsonBuilder().setPrettyPrinting().create();

	public static void addJsonElement(String key, JsonElement element, String... path) {
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
					e.printStackTrace();
					overwrite = true;
				}
			}
		}

		BleachFileMang.createEmptyFile(path);
		if (overwrite) {
			JsonObject mainJO = new JsonObject();
			mainJO.add(key, element);

			BleachFileMang.appendFile(jsonWriter.toJson(mainJO), path);
		} else {
			file.add(key, element);

			BleachFileMang.appendFile(jsonWriter.toJson(file), path);
		}
	}

	public static void setJsonFile(JsonObject element, String... path) {
		BleachFileMang.createEmptyFile(path);
		BleachFileMang.appendFile(jsonWriter.toJson(element), path);
	}

	public static JsonElement readJsonElement(String key, String... path) {
		JsonObject jo = readJsonFile(path);

		if (jo == null)
			return null;

		if (jo.has(key)) {
			return jo.get(key);
		}

		return null;
	}

	public static JsonObject readJsonFile(String... path) {
		List<String> lines = BleachFileMang.readFileLines(path);

		if (lines.isEmpty())
			return null;

		String merged = String.join("\n", lines);

		try {
			return new JsonParser().parse(merged).getAsJsonObject();
		} catch (JsonParseException e) {
			System.err.println("Json error Trying to read " + Arrays.asList(path) + "! DELETING ENTIRE FILE!");
			e.printStackTrace();

			BleachFileMang.deleteFile(path);
			return null;
		}
	}
}
