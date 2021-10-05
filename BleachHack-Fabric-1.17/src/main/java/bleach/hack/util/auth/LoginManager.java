/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util.auth;

import java.lang.reflect.Field;
import java.net.Proxy;
import java.net.URL;

import bleach.hack.mixin.AccessorMinecraftClient;
import com.mojang.authlib.Agent;
import com.mojang.authlib.Environment;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;

import bleach.hack.util.FabricReflect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Session;

public final class LoginManager {

	public static Session loginWithMojang(String email, String password) throws AuthenticationException {
		Session session = createSession(email, password);
		FabricReflect.writeField(MinecraftClient.getInstance(), session, "field_1726", "session");
		return session;
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

	public static Session createAlteningSession(String token) throws Exception {
		MinecraftClient mc = MinecraftClient.getInstance();
		YggdrasilMinecraftSessionService service = (YggdrasilMinecraftSessionService) mc.getSessionService();
		String AUTH = "http://authserver.thealtening.com";
		String ACCOUNT = "https://api.mojang.com";
		String SESSION = "http://sessionserver.thealtening.com";
		String SERVICES = "https://api.minecraftservices.com";

		getField(service, "baseUrl").set(service, SESSION + "/session/minecraft/");
		getField(service, "joinUrl").set(service, new URL(SESSION + "/session/minecraft/join"));
		getField(service, "checkUrl").set(service, new URL(SESSION + "/session/minecraft/hasJoined"));

		Environment environment = Environment.create(AUTH, ACCOUNT, SESSION, SERVICES, "The Altening");
		YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(
				Proxy.NO_PROXY, "", environment).createUserAuthentication(Agent.MINECRAFT);

		auth.setUsername(token);
		auth.setPassword("Password");
		auth.logIn();

		Session session = new Session(auth.getSelectedProfile().getName(),
				auth.getSelectedProfile().getId().toString(),
				auth.getAuthenticatedToken(), "mojang");

		((AccessorMinecraftClient) mc).setSession(session);
		mc.getSessionProperties().clear();

		return session;
	}

	public static Field getField(YggdrasilMinecraftSessionService service, String name) throws Exception {
		Field field = service.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}
}
