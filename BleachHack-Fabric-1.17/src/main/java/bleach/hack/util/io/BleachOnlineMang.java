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
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import bleach.hack.util.BleachLogger;

/**
 * Utils for online BleachHack Resources.
 */
public class BleachOnlineMang {

	private static HttpClient httpClient = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();
	private static URI resourceUrl = URI.create("https://raw.githubusercontent.com/BleachDrinker420/BH-resources/main/");
	private static URI apiUrl = URI.create("http://api.bleachhack.org/");

	public static HttpClient getHttpClient() {
		return httpClient;
	}

	public static URI getResourceUrl() {
		return resourceUrl;
	}

	public static URI getApiUrl() {
		return apiUrl;
	}

	public static <T> T getResource(String path, BodyHandler<T> handler) {
		BleachLogger.logger.info("Getting Resource (/" + path + ")");
		return sendRequest(resourceUrl.resolve(path), "GET", null, 5000, handler);
	}
	
	public static <T> CompletableFuture<T> getResourceAsync(String path, BodyHandler<T> handler) {
		BleachLogger.logger.info("Getting Resource (/" + path + ")");
		return sendAsyncRequest(resourceUrl.resolve(path), "GET", null, 5000, handler);
	}

	public static <T> T sendApiGet(String path, BodyHandler<T> handler) {
		BleachLogger.logger.info("Trying to call API (GET, /" + path + ")");
		return sendRequest(apiUrl.resolve(path), "GET", null, 5000, handler);
	}

	public static <T> T sendApiPost(String path, String body, BodyHandler<T> handler) {
		BleachLogger.logger.info("Trying to call API (POST, /" + path + ", " + body.toString() + ")");
		return sendRequest(apiUrl.resolve(path), "POST", body, 5000, handler);
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