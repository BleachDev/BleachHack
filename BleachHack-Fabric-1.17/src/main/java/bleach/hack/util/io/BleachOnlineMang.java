/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util.io;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import bleach.hack.util.BleachLogger;

/**
 * Utils for online BleachHack Resources.
 */
public class BleachOnlineMang {

	private static URI resourceUrl = URI.create("https://res.bleachhack.org/");
	private static URI apiUrl = URI.create("http://api.bleachhack.org/"); // http because epic amazon fail

	public static URI getResourceUrl() {
		return resourceUrl;
	}

	public static URI getApiUrl() {
		return apiUrl;
	}

	public static List<String> readResourceLines(String path) {
		BleachLogger.logger.info("Getting Resource (/" + path + ")");
		return readLines(createConnection(resourceUrl.resolve(path), "GET", null, 10000));
	}

	public static JsonObject readResourceJson(String path) {
		BleachLogger.logger.info("Getting Resource (/" + path + ")");
		String s = read(createConnection(resourceUrl.resolve(path), "GET", null, 10000));

		if (s != null) {
			try {
				return new JsonParser().parse(s).getAsJsonObject();
			} catch (Exception e) {
				BleachLogger.logger.error("Error parsing json from resource: /" + path, e);
			}
		}

		return null;
	}

	public static String apiGet(String path) {
		BleachLogger.logger.info("Trying to call API (GET, /" + path + ")");
		return read(createConnection(apiUrl.resolve(path), "GET", null, 10000));
	}

	public static String apiPost(String path, JsonElement body) {
		BleachLogger.logger.info("Trying to call API (POST, /" + path + ", " + body.toString() + ")");
		return read(createConnection(apiUrl.resolve(path), "POST", body.toString(), 10000));
	}

	private static HttpURLConnection createConnection(URI url, String method, String body, int timeout) {
		try {
			HttpURLConnection con = (HttpURLConnection) url.toURL().openConnection();
			con.setRequestMethod(method);
			con.setConnectTimeout(timeout);
			con.setReadTimeout(timeout);

			if (body != null && !body.isEmpty()) {
				con.setDoOutput(true);
				con.getOutputStream().write(body.toString().getBytes(StandardCharsets.UTF_8));
			}

			return con;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String read(HttpURLConnection con) {
		try {
			return IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			BleachLogger.logger.error(e);
			return null;
		}
	}

	private static List<String> readLines(HttpURLConnection con) {
		try {
			return IOUtils.readLines(con.getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			BleachLogger.logger.error(e);
			return null;
		}
	}
}