/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.bleachhack.BleachHack;
import org.bleachhack.command.Command;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.io.BleachFileHelper;

import com.google.gson.JsonElement;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.pipe.PipeStatus;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;

import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;

public class DiscordRPC extends Module {

	private IPCClient client;
	private Thread startThread;

	private String customText1 = "top text";
	private String customText2 = "bottom text";

	private long startTime;
	private int tick;

	public DiscordRPC() {
		super("DiscordRPC", KEY_UNBOUND, ModuleCategory.MISC, true, "Discord RPC, use the " + Command.getPrefix() + "rpc command to set a custom status.",
				new SettingMode("Line1", "Playing %server%", "%server%", "%type%", "%username% ontop", "Minecraft %mcver%", "%username%", "<- bad client", "%custom%").withDesc("The top line."),
				new SettingMode("Line2", "%hp% hp - Holding %item%", "%username% - %hp% hp", "Holding %item%", "%hp% hp - At %coords%", "At %coords%", "%custom%").withDesc("The bottom line."),
				new SettingMode("Elapsed", "Normal", "Random", "Backwards", "None").withDesc("How to show elapsed time"),
				new SettingToggle("Silent", false).withDesc("Use a generic Minecraft title and image."));

		JsonElement t1 = BleachFileHelper.readMiscSetting("discordRPCTopText");
		JsonElement t2 = BleachFileHelper.readMiscSetting("discordRPCBottomText");

		if (t1 != null) {
			customText1 = t1.getAsString();
		}

		if (t2 != null) {
			customText2 = t2.getAsString();
		}
	}

	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);

		tick = 0;
		startTime = System.currentTimeMillis();

		if (client == null) {
			BleachLogger.logger.info("Initing Discord IPC...");
			client = new IPCClient(740928841433743370L);
			client.setListener(new IPCListener() {
				@Override
				public void onReady(IPCClient client) {
					BleachLogger.logger.info("Connected to Discord!");
				}
			});
		}

		startThread = new Thread(() -> {
			try {
				client.connect();
			} catch (NoDiscordClientException e) {
				BleachLogger.error("Failed to connect to Discord!");
				setEnabled(false);
			}
		});
		startThread.start();
	}

	@Override
	public void onDisable(boolean inWorld) {
		try {
			startThread.join();
			disconnect();
		} catch (InterruptedException ignored) {}

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (client.getStatus() != PipeStatus.CONNECTED)
			return;

		if (tick % 40 == 0) {
			boolean silent = getSetting(3).asToggle().getState();
			RichPresence.Builder builder = new RichPresence.Builder()
					.setLargeImage(silent ? "mc" : "dh", silent ? "Minecraft " + SharedConstants.getGameVersion().getName() : "DarkHack " + BleachHack.VERSION);

			// Top text
			builder.setDetails(switch (getSetting(0).asMode().getMode()) {
				case 0 ->"Playing " + (mc.getCurrentServerEntry() == null ? "Singleplayer" : mc.getCurrentServerEntry().address);
				case 1 -> mc.getCurrentServerEntry() == null ? "Singleplayer" : mc.getCurrentServerEntry().address;
				case 2 -> mc.getCurrentServerEntry() == null ? "Singleplayer" : "Multiplayer";
				case 3 -> mc.player.getEntityName() + " Ontop!";
				case 4 -> "Minecraft " + SharedConstants.getGameVersion().getName();
				case 5 -> mc.player.getEntityName();
				case 6 -> "<- bad client";
				default -> customText1;
			});

			// Bottom text
			ItemStack currentItem = mc.player.getInventory().getMainHandStack();

			String customName = StringUtils.strip(currentItem.getName().getString());
			if (customName.length() > 25) {
				customName = customName.substring(0, 23) + "..";
			}

			String name = currentItem.getItem().getName().getString();
			String itemName = currentItem.isEmpty() ? "Nothing"
					: (currentItem.getCount() > 1 ? currentItem.getCount() + " " : "")
					+ (currentItem.hasCustomName() ? customName : name);

			builder.setState(switch (getSetting(1).asMode().getMode()) {
				case 0 -> (int) mc.player.getHealth() + " hp - Holding " + itemName;
				case 1 -> mc.player.getEntityName() + " - " + (int) mc.player.getHealth() + " hp";
				case 2 -> "Holding " + itemName;
				case 3 -> (int) mc.player.getHealth() + " hp - At " + mc.player.getBlockPos().toShortString();
				case 4 -> "At " + mc.player.getBlockPos().toShortString();
				default -> customText2;
			});

			// Start time
			if (getSetting(2).asMode().getMode() != 3) {
				long time = switch (getSetting(2).asMode().getMode()) {
					case 1 -> System.currentTimeMillis() - RandomUtils.nextInt(0, 86400000);
					case 2 -> System.currentTimeMillis() - 86400000L + (long) tick * 50;
					default -> startTime;
				};

				builder.setStartTimestamp(OffsetDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.systemDefault()));
			}

			// Build
			client.sendRichPresence(builder.build());
		}

		tick++;
	}

	private void disconnect() {
		if (client.getStatus() == PipeStatus.CONNECTED || client.getStatus() == PipeStatus.CONNECTING)
			client.close();
	}

	public void setTopText(String text) {
		customText1 = text;
	}

	public void setBottomText(String text) {
		customText2 = text;
	}

	public String getTopText() {
		return customText1;
	}

	public String getBottomText() {
		return customText2;
	}
}
