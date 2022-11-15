/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.auth;

import java.net.Proxy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.io.BleachOnlineMang;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import net.minecraft.client.util.Session;

public final class LoginHelper {

	// Microsoft things
	private static final URI MS_OAUTH_URL = URI.create("https://sisu.xboxlive.com/connect/XboxLive/?state=login&cobrandId=8058f65d-ce06-4c30-9559-473c9275a65d&tid=896928775&ru=https://www.minecraft.net/en-us/login");
	private static final URI MC_AUTH_URL = URI.create("https://api.minecraftservices.com/authentication/login_with_xbox");

	private static final Pattern MS_PPFT_PATTERN = Pattern.compile("PPFT\".*?value=\"(.*?)\"");
	private static final Pattern MS_LOGIN_PATTERN = Pattern.compile("urlPostMsa:'(.*?)'");
	private static final Pattern MS_REDIRECT_PATTERN = Pattern.compile("urlPost:'(.*?)'");
	private static final Pattern MS_ACCESS_TOKEN_PATTERN = Pattern.compile("accessToken=(.*?)(&|$)");

	public static Session createMojangSession(String email, String password) throws AuthenticationException {
		YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(
				Proxy.NO_PROXY, "").createUserAuthentication(Agent.MINECRAFT);

		auth.setUsername(email);
		auth.setPassword(password);

		try {
			auth.logIn();
		} catch (AuthenticationException e) {
			if (e.getMessage().toLowerCase(Locale.ENGLISH).contains("credentials"))
				throw new AuthenticationException("Invalid Password!");

			if (e.getMessage().toLowerCase(Locale.ENGLISH).contains("410"))
				throw new AuthenticationException("Account Migrated, Use Microsoft Login.");

			throw e;
		}

		return new Session(auth.getSelectedProfile().getName(),
				auth.getSelectedProfile().getId().toString(),
				auth.getAuthenticatedToken(),
				Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);
	}

	public static Session createMicrosoftSession(String email, String password) throws AuthenticationException {
		JsonObject xsts = getXboxToken(email, password).get(1).getAsJsonObject().get("Item2").getAsJsonObject();
		return getSessionFromXsts(
				xsts.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString(),
				xsts.get("Token").getAsString());
	}

	// [XBL Token, XSTS Token, Playfab Token]
	private static JsonArray getXboxToken(String email, String password) throws AuthenticationException {
		// Generate a Microsoft OAuth token
		HttpResponse<String> oauthResponse = BleachOnlineMang.sendRequest(MS_OAUTH_URL, "GET", null, null, 5000, BodyHandlers.ofString(StandardCharsets.UTF_8));

		throwIfInvalid(oauthResponse, true, "Failed to generate an OAuth token!");

		Matcher ppftMatcher = MS_PPFT_PATTERN.matcher(oauthResponse.body());
		if (!ppftMatcher.find())
			throw new AuthenticationException("Failed to find a PPFT token!");

		Matcher loginMatcher = MS_LOGIN_PATTERN.matcher(oauthResponse.body());
		if (!loginMatcher.find())
			throw new AuthenticationException("Failed to find the login url!");

		String cookie = oauthResponse.headers().allValues("Set-Cookie").stream()
				.filter(s -> s.startsWith("MSPOK="))
				.map(s -> s.substring(0, s.indexOf(';')))
				.findFirst().get();

		// Login to Microsoft
		HttpResponse<String> loginResponse = BleachOnlineMang.sendRequest(
				URI.create(loginMatcher.group(1)),
				"POST",
				new String[] {
						"Content-Type", "application/x-www-form-urlencoded",
						"Cookie", cookie },
				"login=" + URLEncoder.encode(email, StandardCharsets.UTF_8) +
				"&passwd=" + URLEncoder.encode(password, StandardCharsets.UTF_8) +
				"&PPFT=" + URLEncoder.encode(ppftMatcher.group(1), StandardCharsets.UTF_8),
				5000,
				BodyHandlers.ofString(StandardCharsets.UTF_8));

		throwIfInvalid(loginResponse, true, "Failed to login!");

		Matcher redirectMatcher = MS_REDIRECT_PATTERN.matcher(loginResponse.body());
		if (!redirectMatcher.find())
			throw new AuthenticationException("Failed to find the MS redirect link!");

		cookie = loginResponse.headers().allValues("Set-Cookie").stream()
				.map(s -> s.substring(0, s.indexOf(';')))
				.filter(s -> !s.endsWith("= ") && !s.endsWith("="))
				.collect(Collectors.joining("; "));

		// Get Redirect page to the access token
		HttpResponse<String> redirectResponse = BleachOnlineMang.sendRequest(
				URI.create(redirectMatcher.group(1)),
				"POST",
				new String[] {
						"Content-Type", "application/x-www-form-urlencoded",
						"Cookie", cookie },
				"PPFT=" + URLEncoder.encode(ppftMatcher.group(1), StandardCharsets.UTF_8) +
				"&type=28",
				5000,
				BodyHandlers.ofString(StandardCharsets.UTF_8));

		throwIfInvalid(redirectResponse, false, "Failed to get access token from account!");

		// Get Access Token from page
		Matcher accessMatcher = MS_ACCESS_TOKEN_PATTERN.matcher(redirectResponse.uri().toString());
		if (!accessMatcher.find())
			throw new AuthenticationException("Didn't redirect to access token!");

		return JsonParser.parseString(new String(Base64.getDecoder().decode(accessMatcher.group(1)))).getAsJsonArray();
	}

	private static Session getSessionFromXsts(String xstsId, String xstsToken) throws AuthenticationException {
		HttpResponse<String> mcResponse = BleachOnlineMang.sendRequest(
				MC_AUTH_URL,
				"POST",
				new String[] {
						"Content-Type", "application/json",
						"Accept", "application/json" },
				"{\"identityToken\":\"XBL3.0 x=" + xstsId + ";" + xstsToken + "\",\"ensureLegacyEnabled\":true}",
				5000,
				BodyHandlers.ofString(StandardCharsets.UTF_8));

		throwIfInvalid(mcResponse, true, "Failed to get MC token!");

		String mcToken = JsonParser.parseString(mcResponse.body()).getAsJsonObject().get("access_token").getAsString();

		HttpResponse<String> profileResponse = BleachOnlineMang.sendRequest(
				URI.create("https://api.minecraftservices.com/minecraft/profile"),
				"GET",
				new String[] { "Authorization", "Bearer " + mcToken },
				null,
				5000,
				BodyHandlers.ofString(StandardCharsets.UTF_8));

		throwIfInvalid(profileResponse, true, "Failed to get MC profile!");

		JsonObject profileJson = JsonParser.parseString(profileResponse.body()).getAsJsonObject();

		if (!profileJson.has("id"))
			throw new AuthenticationException("Got invalid MC profile!");

		String id = profileJson.get("id").getAsString();

		if (id.length() == 32)
			id = id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20);

		return new Session(profileJson.get("name").getAsString(), id, mcToken, Optional.empty(), Optional.empty(), Session.AccountType.MSA);
	}

	private static void throwIfInvalid(HttpResponse<?> response, boolean checkStatus, String reason) throws AuthenticationException {
		if (response == null || (checkStatus && (response.statusCode() < 200 || response.statusCode() >= 300))) {
			BleachLogger.logger.error("> Response: " + response);
			if (response != null) {
				BleachLogger.logger.error("> Headers: " + response.headers());
				BleachLogger.logger.error("> Body: " + response.body());
			}

			AuthenticationException authEx = new AuthenticationException(reason);
			authEx.printStackTrace();
			throw authEx;
		}
	}
}
