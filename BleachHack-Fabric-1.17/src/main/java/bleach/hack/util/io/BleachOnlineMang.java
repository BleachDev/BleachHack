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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import bleach.hack.util.BleachLogger;

/**
 * Utils for online BleachHack Resources.
 */
public class BleachOnlineMang {

	private static HttpClient httpClient = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();
	private static URI resourceUrl = URI.create("https://res.bleachhack.org/");
	private static URI apiUrl = URI.create("https://api.bleachhack.org/");

	public static void warmUp() {
		// Caches the dns of the urls
		sendAsyncRequest(resourceUrl, "GET", null, 5000, BodyHandlers.discarding());
		sendAsyncRequest(apiUrl, "GET", null, 5000, BodyHandlers.discarding());
	}

	public static HttpClient getHttpClient() {
		return httpClient;
	}

	public static URI getResourceUrl() {
		return resourceUrl;
	}

	public static URI getApiUrl() {
		return apiUrl;
	}

	public static List<String> readResourceLines(String path) {
		BleachLogger.logger.info("Getting Resource (/" + path + ")");
		return sendRequest(resourceUrl.resolve(path), "GET", null, 5000, BodyHandlers.ofLines()).collect(Collectors.toList());
	}

	public static JsonObject readResourceJson(String path) {
		BleachLogger.logger.info("Getting Resource (/" + path + ")");
		String s = sendRequest(resourceUrl.resolve(path), "GET", null, 5000, BodyHandlers.ofString());

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
		return sendRequest(apiUrl.resolve(path), "GET", null, 5000, BodyHandlers.ofString());
	}

	public static String apiPost(String path, JsonElement body) {
		BleachLogger.logger.info("Trying to call API (POST, /" + path + ", " + body.toString() + ")");
		return sendRequest(apiUrl.resolve(path), "POST", body.toString(), 5000, BodyHandlers.ofString());
	}

	private static <T> T sendRequest(URI url, String method, String body, int timeout, BodyHandler<T> handler) {
		try {
			return httpClient.send(
					HttpRequest
					.newBuilder(url)
					.timeout(Duration.ofMillis(timeout))
					.method(method, body != null ? BodyPublishers.ofString(body) : BodyPublishers.noBody())
					.build(), handler)
					.body();
		} catch (IOException | InterruptedException e) {
			BleachLogger.logger.error(e);
			return null;
		}
	}

	private static <T> CompletableFuture<T> sendAsyncRequest(URI url, String method, String body, int timeout, BodyHandler<T> handler) {
		return httpClient.sendAsync(
				HttpRequest
				.newBuilder(url)
				.timeout(Duration.ofMillis(timeout))
				.method(method, body != null ? BodyPublishers.ofString(body) : BodyPublishers.noBody())
				.build(), handler)
				.thenApply(HttpResponse::body);
	}
}