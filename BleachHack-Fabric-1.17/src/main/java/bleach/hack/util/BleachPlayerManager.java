/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import bleach.hack.util.io.BleachAPIMang;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

public class BleachPlayerManager {

	private ScheduledExecutorService pingExecutor;
	private ScheduledExecutorService playerExecutor;

	private Set<UUID> players = new HashSet<>();
	private Set<UUID> playerQueue = new HashSet<>();

	public BleachPlayerManager() {
		playerExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).build());
		playerExecutor.scheduleAtFixedRate(() -> {
			if (!playerQueue.isEmpty()) {
				JsonArray playersJson = new JsonArray();
				playerQueue.forEach(p -> playersJson.add(p.toString().replace("-", "")));
				playerQueue.clear();

				String response = BleachAPIMang.post("online", playersJson);

				try {
					new JsonParser().parse(response).getAsJsonArray().forEach(j -> players.add(UUID.fromString(j.getAsString())));
				} catch (Exception ignored) { }
			}
		}, 0L, 10L, TimeUnit.SECONDS);

		playerExecutor.scheduleAtFixedRate(() -> {
			ClientPlayerEntity player = MinecraftClient.getInstance().player;

			if (player == null) {
				players.clear();
				return;
			}

			players.removeIf(p -> !player.networkHandler.getPlayerUuids().contains(p));

			if (!players.isEmpty()) {
				JsonArray playersJson = new JsonArray();
				players.forEach(p -> playersJson.add(p.toString().replace("-", "")));

				String response = BleachAPIMang.post("online", playersJson);

				try {
					new JsonParser().parse(response).getAsJsonArray().forEach(j -> players.add(UUID.fromString(j.getAsString())));
				} catch (Exception ignored) { }

				players.clear();
			}
		}, 0L, 10L, TimeUnit.MINUTES);
	}

	public void startPinger() {
		if (pingExecutor == null || pingExecutor.isShutdown()) {
			pingExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).build());
			pingExecutor.scheduleAtFixedRate(() -> BleachAPIMang.get("online/ping?uuid=" + MinecraftClient.getInstance().getSession().getUuid().replace("-", "")), 0L, 14L, TimeUnit.MINUTES);
		}
	}

	public void stopPinger() {
		if (pingExecutor != null && !pingExecutor.isShutdown()) {
			pingExecutor.execute(() -> BleachAPIMang.get("online/disconnect?uuid=" + MinecraftClient.getInstance().getSession().getUuid().replace("-", "")));
			try {
				pingExecutor.awaitTermination(15L, TimeUnit.SECONDS);
			} catch (InterruptedException ignored) { }
		}
	}

	public void addQueueEntries(Collection<PlayerListS2CPacket.Entry> entries) {
		UUID playerUuid = UUID.fromString(MinecraftClient.getInstance().getSession().getUuid());
		entries.stream()
		.map(e -> e.getProfile().getId())
		.filter(e -> !players.contains(e))
		.forEach(e -> {
			if (playerUuid.equals(e)) {
				players.add(e);
			} else {
				playerQueue.add(e);
			}
		});
	}

	public void removeQueueEntries(Collection<PlayerListS2CPacket.Entry> entries) {
		entries.forEach(e -> playerQueue.remove(e.getProfile().getId()));
	}

	public Set<UUID> getPlayers() {
		return players;
	}
}
