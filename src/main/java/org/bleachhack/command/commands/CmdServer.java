/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import net.minecraft.SharedConstants;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import org.bleachhack.BleachHack;
import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.command.exception.CmdSyntaxException;
import org.bleachhack.event.events.EventPacket;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.util.BleachLogger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CmdServer extends Command {

	public CmdServer() {
		super("server", "Server things.", "server address | server brand | server day | server difficulty | server ip | server motd | server ping | server permissions | server plugins | server protocol | server version", CommandCategory.MISC);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		boolean sp = mc.isIntegratedServerRunning();

		if (!sp && mc.getCurrentServerEntry() == null) {
			BleachLogger.error("Unable to get server info.");
			return;
		}

		BleachLogger.info("Server Info");

		if (args.length == 0) {
			BleachLogger.noPrefix(createText("Address", getAddress(sp)));
			BleachLogger.noPrefix(createText("Brand", getBrand(sp)));
			BleachLogger.noPrefix(createText("Day", getDay(sp)));
			BleachLogger.noPrefix(createText("Difficulty", getDifficulty(sp)));
			BleachLogger.noPrefix(createText("IP", getIP(sp)));
			BleachLogger.noPrefix(createText("Motd", getMotd(sp)));
			BleachLogger.noPrefix(createText("Ping", getPing(sp)));
			BleachLogger.noPrefix(createText("Permission Level", getPerms(sp)));
			BleachLogger.noPrefix(createText("Protocol", getProtocol(sp)));
			BleachLogger.noPrefix(createText("Version", getVersion(sp)));
			checkForPlugins();
		} else if (args[0].equalsIgnoreCase("address")) {
			BleachLogger.noPrefix(createText("Address", getAddress(sp)));
		} else if (args[0].equalsIgnoreCase("brand")) {
			BleachLogger.noPrefix(createText("Brand", getBrand(sp)));
		} else if (args[0].equalsIgnoreCase("day")) {
			BleachLogger.noPrefix(createText("Day", getDay(sp)));
		} else if (args[0].equalsIgnoreCase("difficulty")) {
			BleachLogger.noPrefix(createText("Difficulty", getDifficulty(sp)));
		} else if (args[0].equalsIgnoreCase("ip")) {
			BleachLogger.noPrefix(createText("IP", getIP(sp)));
		} else if (args[0].equalsIgnoreCase("motd")) {
			BleachLogger.noPrefix(createText("Motd", getMotd(sp)));
		} else if (args[0].equalsIgnoreCase("ping")) {
			BleachLogger.noPrefix(createText("Ping", getPing(sp)));
		} else if (args[0].equalsIgnoreCase("permissions")) {
			BleachLogger.noPrefix(createText("Permission Level", getPerms(sp)));
		} else if (args[0].equalsIgnoreCase("plugins")) {
			checkForPlugins();
		} else if (args[0].equalsIgnoreCase("protocol")) {
			BleachLogger.noPrefix(createText("Protocol", getProtocol(sp)));
		} else if (args[0].equalsIgnoreCase("version")) {
			BleachLogger.noPrefix(createText("Version", getVersion(sp)));
		} else {
			throw new CmdSyntaxException("Invalid server bruh.");
		}
	}

	@BleachSubscribe
	public void onReadPacket(EventPacket.Read event) {
		if (event.getPacket() instanceof CommandSuggestionsS2CPacket) {
			BleachHack.eventBus.unsubscribe(this);

			CommandSuggestionsS2CPacket packet = (CommandSuggestionsS2CPacket) event.getPacket();
			List<String> plugins = packet.getSuggestions().getList().stream()
					.map(s -> {
						String[] split = s.getText().split(":");
						return split.length != 1 ? split[0].replace("/", "") : null;
					})
					.filter(Objects::nonNull)
					.distinct()
					.sorted()
					.collect(Collectors.toList());

			if (!plugins.isEmpty()) {
				BleachLogger.noPrefix(createText("Plugins \u00a7f(" + plugins.size() + ")", "\u00a7a" + String.join("\u00a7f, \u00a7a", plugins)));
			} else {
				BleachLogger.noPrefix("\u00a7cNo plugins found");
			}
		}
	}

	public Text createText(String name, String value) {
		boolean newlines = value.contains("\n");
		return Text.literal("\u00a77" + name + "\u00a7f:" + (newlines ? "\n" : " " ) + "\u00a7a" + value).styled(style -> style
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to copy to clipboard")))
				.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, Formatting.strip(value))));
	}

	public void checkForPlugins() {
		BleachHack.eventBus.subscribe(this); // Plugins
		mc.player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(0, "/"));

		Thread timeoutThread = new Thread(() -> {
			try {
				Thread.sleep(5000);
				if (BleachHack.eventBus.unsubscribe(this))
					BleachLogger.noPrefix("\u00a7cPlugin check timed out");
			} catch (InterruptedException ignored) {
			}
		});
		timeoutThread.setDaemon(true);
		timeoutThread.start();
	}

	public String getAddress(boolean singleplayer) {
		if (singleplayer)
			return "Singleplayer";

		return mc.getCurrentServerEntry().address != null ? mc.getCurrentServerEntry().address : "Unknown";
	}

	public String getBrand(boolean singleplayer) {
		if (singleplayer)
			return "Integrated Server";

		return mc.player.getServerBrand() != null ? mc.player.getServerBrand() : "Unknown";
	}

	public String getDay(boolean singleplayer) {
		return "Day " + (mc.world.getTimeOfDay() / 24000L);
	}

	public String getDifficulty(boolean singleplayer) {
		return StringUtils.capitalize(mc.world.getDifficulty().getName()) + " (Local: " + mc.world.getLocalDifficulty(mc.player.getBlockPos()).getLocalDifficulty() + ")";
	}

	public String getIP(boolean singleplayer) {
		try {
			if (singleplayer)
				return InetAddress.getLocalHost().getHostAddress();

			return mc.getCurrentServerEntry().address != null ? InetAddress.getByName(mc.getCurrentServerEntry().address).getHostAddress() : "Unknown";
		} catch (UnknownHostException e) {
			return "Unknown";
		}
	}

	public String getMotd(boolean singleplayer) {
		if (singleplayer)
			return "-";

		return mc.getCurrentServerEntry().label != null ? mc.getCurrentServerEntry().label.getString() : "Unknown";
	}

	public String getPing(boolean singleplayer) {
		PlayerListEntry playerEntry = mc.player.networkHandler.getPlayerListEntry(mc.player.getGameProfile().getId());
		return playerEntry == null ? "0" : Integer.toString(playerEntry.getLatency());
	}

	public String getPerms(boolean singleplayer) {
		int p = 0;
		while (mc.player.hasPermissionLevel(p + 1) && p < 5) p++;

		return switch (p) {
			case 0 -> "0 (No Perms)";
			case 1 -> "1 (No Perms)";
			case 2 -> "2 (Player Command Access)";
			case 3 -> "3 (Server Command Access)";
			case 4 -> "4 (Operator)";
			default -> p + " (Unknown)";
		};
	}

	public String getProtocol(boolean singleplayer) {
		if (singleplayer)
			return Integer.toString(SharedConstants.getProtocolVersion());

		return Integer.toString(mc.getCurrentServerEntry().protocolVersion);
	}

	public String getVersion(boolean singleplayer) {
		if (singleplayer)
			return SharedConstants.getGameVersion().getName();

		return mc.getCurrentServerEntry().version != null ? mc.getCurrentServerEntry().version.getString() : "Unknown (" + SharedConstants.getGameVersion().getName() + ")";
	}
}
