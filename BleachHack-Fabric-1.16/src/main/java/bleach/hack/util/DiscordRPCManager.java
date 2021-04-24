/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util;

import bleach.hack.BleachHack;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;

public class DiscordRPCManager {

	public static void start(String id) {
		BleachHack.logger.info("Initing Discord RPC...");

		DiscordRPC.discordInitialize(id, new DiscordEventHandlers.Builder().setReadyEventHandler(user -> {
			BleachHack.logger.info(user.username + "#" + user.discriminator + " is big gay");
		}).build(), true);
	}

	public static void stop() {
		DiscordRPC.discordShutdown();
	}
}
