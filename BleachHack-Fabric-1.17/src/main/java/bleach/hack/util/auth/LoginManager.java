/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util.auth;

import java.net.Proxy;
import java.util.Locale;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import bleach.hack.util.FabricReflect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;

public final class LoginManager {

	public static Pair<String, Session> login(String email, String password) {
		if (email.isEmpty())
			return Pair.of("\u00a7cNo Username/Email!", null);

		if (password.isEmpty()) {
			FabricReflect.writeField(MinecraftClient.getInstance().getSession(), email, "field_1982", "username");
			return Pair.of("\u00a76Logged in as an unverified account", null); /* Idk this sound weird */
		}

		YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(
				Proxy.NO_PROXY, "").createUserAuthentication(Agent.MINECRAFT);

		auth.setUsername(email);
		auth.setPassword(password);

		try {
			Session session = createSession(email, password);
			FabricReflect.writeField(MinecraftClient.getInstance(), session, "field_1726", "session");
			return Pair.of("\u00a7aLogin Successful", session);

		} catch (AuthenticationException e) {
			e.printStackTrace();

			if (e.getMessage().toLowerCase(Locale.ENGLISH).contains("invalid username or password") || e.getMessage().toLowerCase(Locale.ENGLISH).contains("account migrated")) {
				return Pair.of("\u00a74Wrong password!", null);
			} else {
				return Pair.of("\u00a7cCannot contact authentication server!", null);
			}

		} catch (NullPointerException e) {
			return Pair.of("\u00a74Wrong password!", null);
		}
	}

	public static Session createSessionSilent(String email, String password) {
		try {
			return createSession(email, password);
		} catch (AuthenticationException e) {
			return null;
		}
	}

	public static Session createSession(String email, String password) throws AuthenticationException {
		YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(
				Proxy.NO_PROXY, "").createUserAuthentication(Agent.MINECRAFT);

		auth.setUsername(email);
		auth.setPassword(password);
		auth.logIn();

		return new Session(auth.getSelectedProfile().getName(),
				auth.getSelectedProfile().getId().toString(),
				auth.getAuthenticatedToken(), "mojang");
	}
}
