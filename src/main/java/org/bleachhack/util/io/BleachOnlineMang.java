/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.io;

import org.bleachhack.util.BleachLogger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Utils for online BleachHack Resources.
 */
public class BleachOnlineMang {

	public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).build();
	private static final URI RESOURCE_URL = URI.create("https://bleachhack.org/resources/");
	private static final URI API_URL = URI.create("http://api0.bleachhack.org/"); // using api0 because of compatibility with BH 1.2.1 and under.

	public static URI getResourceUrl() {
		return RESOURCE_URL;
	}

	public static URI getApiUrl() {
		return API_URL;
	}

	public static <T> T getResource(String path, BodyHandler<T> handler) {
		BleachLogger.logger.info("Getting Resource (/" + path + ")");
		return sendRequest(RESOURCE_URL.resolve(path), "GET", null, null, 5000, handler).body();
	}

	public static <T> CompletableFuture<T> getResourceAsync(String path, BodyHandler<T> handler) {
		BleachLogger.logger.info("Getting Resource (/" + path + ")");
		return sendAsyncRequest(RESOURCE_URL.resolve(path), "GET", null, null, 5000, handler).thenApply(HttpResponse::body);
	}

	public static <T> T sendApiGet(String path, BodyHandler<T> handler) {
		BleachLogger.logger.info("Trying to call API (GET, /" + path + ")");
		return sendRequest(API_URL.resolve(path), "GET", null, null, 5000, handler).body();
	}

	public static <T> T sendApiPost(String path, String body, BodyHandler<T> handler) {
		BleachLogger.logger.info("Trying to call API (POST, /" + path + ", " + body + ")");
		return sendRequest(API_URL.resolve(path), "POST", null, body, 5000, handler).body();
	}

	// Raw request methods
	public static <T> HttpResponse<T> sendRequest(URI url, String method, String[] headers, String body, int timeout, BodyHandler<T> handler) {
		return sendRequest(HTTP_CLIENT, url, method, headers, body, timeout, handler);
	}

	public static <T> HttpResponse<T> sendRequest(HttpClient client, URI url, String method, String[] headers, String body, int timeout, BodyHandler<T> handler) {
		try {
			Builder rqBuilder = HttpRequest
					.newBuilder(url)
					.timeout(Duration.ofMillis(timeout))
					.method(method, body != null ? BodyPublishers.ofString(body) : BodyPublishers.noBody());

			if (headers != null)
				rqBuilder.headers(headers);

			return client.send(rqBuilder.build(), handler);
		} catch (IOException | InterruptedException e) {
			BleachLogger.logger.error(e);
			return null;
		}
	}

	public static <T> CompletableFuture<HttpResponse<T>> sendAsyncRequest(URI url, String method, String[] headers, String body, int timeout, BodyHandler<T> handler) {
		return sendAsyncRequest(HTTP_CLIENT, url, method, headers, body, timeout, handler);
	}

	public static <T> CompletableFuture<HttpResponse<T>> sendAsyncRequest(HttpClient client, URI url, String method, String[] headers, String body, int timeout, BodyHandler<T> handler) {
		Builder rqBuilder = HttpRequest
				.newBuilder(url)
				.timeout(Duration.ofMillis(timeout))
				.method(method, body != null ? BodyPublishers.ofString(body) : BodyPublishers.noBody());

		if (headers != null)
			rqBuilder.headers(headers);

		return client.sendAsync(rqBuilder.build(), handler);
	}
}