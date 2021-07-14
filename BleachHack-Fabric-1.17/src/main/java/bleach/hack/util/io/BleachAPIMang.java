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

import org.apache.commons.io.IOUtils;

import com.google.common.io.Resources;
import com.google.gson.JsonElement;

import bleach.hack.util.BleachLogger;

/**
 * Utils for the BleachHack API, used for nonstatic assets (like how many players are online)
 */
public class BleachAPIMang {

	private static URI url = URI.create("http://api.bleachhack.org/");

	public static String get(String path) {
		BleachLogger.logger.info("Trying to call API (GET, /" + path + ")");
		try {
			return Resources.toString(url.resolve(path).toURL(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String post(String path, JsonElement body) {
		BleachLogger.logger.info("Trying to call API (POST, /" + path + ", " + body.toString() + ")");
		try {
			HttpURLConnection con = (HttpURLConnection) url.resolve(path).toURL().openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);

			IOUtils.write(body.toString(), con.getOutputStream(), StandardCharsets.UTF_8);
			return IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}