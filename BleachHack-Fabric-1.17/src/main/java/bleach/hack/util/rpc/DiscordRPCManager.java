/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util.rpc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.lang3.SystemUtils;

import com.sun.jna.Library;
import com.sun.jna.Native;
import bleach.hack.util.BleachLogger;

/**
 * Baked in discord rpc because MC 1.17-pre1 decided to not launch with it as a library.
 * 
 * @author Nicolas "Vatuu" Adamoglou
 * @version 1.5.1
 * <p>
 * Java Wrapper of the Discord-RPC Library for Discord Rich Presence.
 **/
public class DiscordRPCManager {

	private static DiscordLib LIBRARY = loadLibrary();

	private static DiscordLib loadLibrary() {
		try {
			String libName;
			if (SystemUtils.IS_OS_MAC) {
				libName = "discord-rpc-darwin.dylib";
			} else if (SystemUtils.IS_OS_WINDOWS) {
				libName = "discord-rpc-win-" + (System.getProperty("sun.arch.data.model").equals("64") ? "x64" : "x86") + ".dll";
			} else if ("The Android Project".equals(System.getProperty("java.specification.vendor"))) {
				BleachLogger.logger.warn("Appears to be running on android, skipping loading rpc library.");
				return new EmptyDiscordLib();
			} else {
				libName = "linux";
			}

			File file = new File(System.getProperty("java.io.tmpdir"), libName);
			file.mkdirs();

			InputStream is = DiscordRPCManager.class.getResourceAsStream("/assets/bleachhack/rpc/" + libName);
			Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			is.close();

			DiscordLib lib = Native.load(file.getAbsolutePath(), DiscordLib.class);

			BleachLogger.logger.info("Successfully loaded native discord library (/assets/bleachhack/rpc/" + libName + " -> " + file.getAbsolutePath() + ")");
			return lib;
		} catch (IOException e) {
			BleachLogger.logger.error("Discord RPC could not be initialized", e);
			return new EmptyDiscordLib();
		}
	}

	/**
	 * Method to initialize the Discord-RPC.
	 *
	 * @param applicationId ApplicationID/ClientID
	 * @param handlers      EventHandlers
	 */
	public static void initialize(String id, DiscordEventHandlers callback) {
		LIBRARY.Discord_Initialize(id, callback, 1, null);
	}

	/**
	 * Method to register the executable of the application/game.
	 * Only applicable when autoRegister in discordInitialize is false.
	 *
	 * @param applicationId ApplicationID/ClientID
	 * @param command       Launch Command of the application/game.
	 */
	public static void register(String applicationId, String command) {
		LIBRARY.Discord_Register(applicationId, command);
	}

	/**
	 * Method to call Callbacks from within the library.
	 * Must be called periodically.
	 */
	public static void runCallbacks() {
		LIBRARY.Discord_RunCallbacks();
	}

	/**
	 * Method to update the DiscordRichPresence of the client.
	 *
	 * @param presence Instance of DiscordRichPresence
	 * @see DiscordRichPresence
	 */
	public static void updatePresence(DiscordRichPresence presence) {
		LIBRARY.Discord_UpdatePresence(presence);
	}

	/**
	 * Method to clear(and therefor hide) the DiscordRichPresence until a new
	 * presence is applied.
	 */
	public static void clearPresence() {
		LIBRARY.Discord_ClearPresence();
	}

	/**
	 * Method to respond to Join/Spectate Callback.
	 *
	 * @param userId UserID of the user to respond to.
	 * @param reply  DiscordReply to request. (0 = No, 1 = Yes, 2 = Ignore)
	 * @see DiscordReply
	 */
	public static void respond(String userId, int reply) {
		LIBRARY.Discord_Respond(userId, reply);
	}

	public static void shutdown() {
		LIBRARY.Discord_Shutdown();
	}

	//JNA Interface
	private static interface DiscordLib extends Library {

		void Discord_Initialize(String applicationId, DiscordEventHandlers handlers, int autoRegister, String optionalSteamId);
		void Discord_Register(String applicationId, String command);
		void Discord_RegisterSteamGame(String applicationId, String steamId);
		void Discord_UpdateHandlers(DiscordEventHandlers handlers);
		void Discord_Shutdown();
		void Discord_RunCallbacks();
		void Discord_UpdatePresence(DiscordRichPresence presence);
		void Discord_ClearPresence();
		void Discord_Respond(String userId, int reply);
	}

	private static class EmptyDiscordLib implements DiscordLib {

		public void Discord_Initialize(String applicationId, DiscordEventHandlers handlers, int autoRegister, String optionalSteamId) {}
		public void Discord_Register(String applicationId, String command) {}
		public void Discord_RegisterSteamGame(String applicationId, String steamId) {}
		public void Discord_UpdateHandlers(DiscordEventHandlers handlers) {}
		public void Discord_Shutdown() {}
		public void Discord_RunCallbacks() {}
		public void Discord_UpdatePresence(DiscordRichPresence presence) {}
		public void Discord_ClearPresence() {}
		public void Discord_Respond(String userId, int reply) {}

	}
}
