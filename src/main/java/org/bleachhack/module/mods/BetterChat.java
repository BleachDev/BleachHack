/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bleachhack.BleachHack;
import org.bleachhack.command.Command;
import org.bleachhack.event.events.EventReadPacket;
import org.bleachhack.event.events.EventSendPacket;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.module.mods.BetterChat.CustomFont.CharMap;
import org.bleachhack.module.setting.base.SettingMode;
import org.bleachhack.module.setting.base.SettingToggle;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.Texts;
import org.bleachhack.util.io.BleachFileHelper;

import com.github.fzakaria.ascii85.Ascii85;
import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import net.minecraft.network.MessageType;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BetterChat extends Module {

	private static final List<CustomFont> fonts = Arrays.asList(
			new CustomFont(CharMap.range('!', 0xFF01, 95)),
			new CustomFont(CharMap.single('a', '\u1D00'), CharMap.single('b', '\u0299'), CharMap.range('c', 0x1d04, 2), CharMap.single('e', '\u1d07'),
					CharMap.single('f', '\ua730'), CharMap.single('g', '\u0262'), CharMap.single('h', '\u029c'), CharMap.single('i', '\u026a'),
					CharMap.range('j', 0x1D0a, 2), CharMap.single('l', '\u029f'), CharMap.single('m', '\u1d0d'), CharMap.single('n', '\u0274'),
					CharMap.single('o', '\u1D0f'), CharMap.single('p', '\u1d29'), CharMap.single('r', '\u0280'), CharMap.single('s', '\ua731'),
					CharMap.range('t', 0x1D1b, 2), CharMap.range('v', 0x1d20, 2), CharMap.single('z', '\u1d22'),
					CharMap.single('A', '\u1D00'), CharMap.single('B', '\u0299'), CharMap.range('C', 0x1d04, 2), CharMap.single('E', '\u1d07'),
					CharMap.single('F', '\ua730'), CharMap.single('G', '\u0262'), CharMap.single('H', '\u029c'), CharMap.single('I', '\u026a'),
					CharMap.range('J', 0x1D0a, 2), CharMap.single('L', '\u029f'), CharMap.single('M', '\u1d0d'), CharMap.single('N', '\u0274'),
					CharMap.single('O', '\u1D0f'), CharMap.single('P', '\u1d29'), CharMap.single('R', '\u0280'), CharMap.single('S', '\ua731'),
					CharMap.range('T', 0x1D1b, 2), CharMap.range('V', 0x1d20, 2), CharMap.single('z', '\u1d22')),
			new CustomFont(CharMap.range('1', 0x2461, 9), CharMap.range('A', 0x24B6, 26), CharMap.range('a', 0x24D0, 26)),
			new CustomFont(
					CharMap.single('a', '\u039b'), CharMap.single('c', '\u1455'), CharMap.single('e', '\u03A3'), CharMap.single('h', '\u0389'),
					CharMap.single('l', '\u14aa'), CharMap.single('n', '\u041f'), CharMap.single('o', '\u04e8'), CharMap.single('r', '\u042f'),
					CharMap.single('s', '\u01a7'), CharMap.single('t', '\u01ac'), CharMap.single('u', '\u0426'), CharMap.single('w', '\u0429'),
					CharMap.single('A', '\u039b'), CharMap.single('C', '\u1455'), CharMap.single('E', '\u03A3'), CharMap.single('H', '\u0389'),
					CharMap.single('L', '\u14aa'), CharMap.single('N', '\u041f'), CharMap.single('O', '\u04e8'), CharMap.single('R', '\u042f'),
					CharMap.single('S', '\u01a7'), CharMap.single('T', '\u01ac'), CharMap.single('U', '\u0426'), CharMap.single('W', '\u0429')),
			new CustomFont(
					CharMap.single('a', '\u03b1'), CharMap.single('b', '\u0432'), CharMap.single('d', '\u2202'), CharMap.single('e', '\u0454'),
					CharMap.single('f', '\u0192'), CharMap.single('h', '\u043d'), CharMap.single('i', '\u03b9'), CharMap.single('j', '\u05e0'),
					CharMap.single('k', '\u043a'), CharMap.single('l', '\u2113'), CharMap.single('m', '\u043c'), CharMap.single('n', '\u03b7'),
					CharMap.single('o', '\u03c3'), CharMap.single('p', '\u03c1'), CharMap.single('r', '\u044f'), CharMap.single('s', '\u0455'),
					CharMap.single('t', '\u0442'), CharMap.single('u', '\u03c5'), CharMap.single('v', '\u03bd'), CharMap.single('w', '\u03c9'),
					CharMap.single('x', '\u03c7'), CharMap.single('y', '\u0443'),
					CharMap.single('A', '\u03b1'), CharMap.single('B', '\u0432'), CharMap.single('D', '\u2202'), CharMap.single('E', '\u0454'),
					CharMap.single('F', '\u0192'), CharMap.single('H', '\u043d'), CharMap.single('I', '\u03b9'), CharMap.single('J', '\u05e0'),
					CharMap.single('K', '\u043a'), CharMap.single('L', '\u2113'), CharMap.single('M', '\u043c'), CharMap.single('N', '\u03b7'),
					CharMap.single('O', '\u03c3'), CharMap.single('P', '\u03c1'), CharMap.single('R', '\u044f'), CharMap.single('S', '\u0455'),
					CharMap.single('T', '\u0442'), CharMap.single('U', '\u03c5'), CharMap.single('V', '\u03bd'), CharMap.single('W', '\u03c9'),
					CharMap.single('X', '\u03c7'), CharMap.single('Y', '\u0443')));

	public String prefix = "";
	public String suffix = " \u25ba \u0432\u029f\u0454\u03b1c\u043d\u043d\u03b1c\u043a";
	public Set<Pattern> filterPatterns = new LinkedHashSet<>();

	public BetterChat() {
		super("BetterChat", KEY_UNBOUND, ModuleCategory.MISC, "Adds more customizability to the chat, use the " + Command.getPrefix() + "betterchat command to edit the stuff.",
				new SettingToggle("CustomFont", false).withDesc("Uses a custom font in your messages.").withChildren(
						new SettingMode("Font", "\uff41\uff42\uff43\uff44\uff45", "\u1D00\u0299\u1d04\u1d05\u1d07",
								"\u24d0\u24d1\u24d2\u24d3\u24d4", "\u039bb\u1455d\u03A3", "\u03b1\u0432c\u2202\u0454").withDesc("The custom font to use.")),
				new SettingToggle("Prefix", false).withDesc("Message prepended to the message, edit with " + Command.getPrefix() + "betterchat prefix."),
				new SettingToggle("Suffix", false).withDesc("Message appended to the message, edit with " + Command.getPrefix() + "betterchat suffix."),
				new SettingToggle("Timestamp", true).withDesc("Adds a timestamp in front of every message.").withChildren(
						new SettingToggle("Seconds", true).withDesc("Shows seconds in the timestamp.")),
				new SettingToggle("Filter", false).withDesc("Filters certain text from the chat, edit the filters with " + Command.getPrefix() + "betterchat filter.").withChildren(
						new SettingMode("Mode", "Censor", "Block", "Remove").withDesc("How to handle filtered messages.")),
				new SettingToggle("ChatEncrypt", false).withDesc("Encrypts messages so only BleachHack users can read them."), //.withChildren(
				// new SettingMode("Mode", "Auto", "Server", "Client").withDesc("Where to store the encrypted the message.")),
				// No backdoor yet :(
				new SettingToggle("ChatDecrypt", true).withDesc("Makes you able to read other peoples encrypted messages."));

		JsonElement pfx = BleachFileHelper.readMiscSetting("betterChatPrefix");
		if (pfx != null && pfx.isJsonPrimitive()) prefix = pfx.getAsString();

		JsonElement sfx = BleachFileHelper.readMiscSetting("betterChatSuffix");
		if (sfx != null && sfx.isJsonPrimitive()) suffix = sfx.getAsString();

		JsonElement filters = BleachFileHelper.readMiscSetting("betterChatFilter");
		if (filters != null && filters.isJsonArray()) {
			for (JsonElement f: filters.getAsJsonArray()) {
				if (f.isJsonPrimitive()) {
					try {
						filterPatterns.add(Pattern.compile(f.getAsString()));
					} catch (PatternSyntaxException | JsonParseException e) {
						BleachLogger.logger.error("Error parsing CustomChat filter pattern: " + f.toString());
					}
				}
			}
		}
	}

	@BleachSubscribe
	public void onPacketSend(EventSendPacket event) {
		if (event.getPacket() instanceof ChatMessageC2SPacket) {
			ChatMessageC2SPacket packet = (ChatMessageC2SPacket) event.getPacket();
			String prefix = "";
			String text = packet.getChatMessage();

			if (text.startsWith("/r ") || text.startsWith("/reply ")) {
				String[] split = text.split(" ");
				prefix = split[0] + " ";
				text = text.substring(prefix.length());
			} else if (text.startsWith("/msg ") || text.startsWith("/tell ") || text.startsWith("/w ") || text.startsWith("/whisper ") || text.startsWith("/pm ")) {
				String[] split = text.split(" ");
				if (split.length >= 3) {
					prefix = split[0] + " " + split[1] + " ";
					text = text.substring(prefix.length());
				}
			} else if (text.startsWith("/") || text.startsWith("!")) {
				return;
			}

			if (getSetting(0).asToggle().state) {
				text = fonts.get(getSetting(0).asToggle().getChild(0).asMode().mode).replace(text);
			}

			if (getSetting(1).asToggle().state) {
				text = this.prefix + text;
			}

			if (getSetting(2).asToggle().state) {
				text = text + this.suffix;
			}

			if (getSetting(5).asToggle().state) {
				String key = getRandomString(4);
				text = encrypt(text, key) + "\u00ff" + key;
			}

			if (!text.equals(packet.getChatMessage())) {
				packet.chatMessage = prefix + text;
			}
		}
	}

	@BleachSubscribe
	public void onPacketRead(EventReadPacket event) {
		if (event.getPacket() instanceof GameMessageS2CPacket) {
			GameMessageS2CPacket packet = (GameMessageS2CPacket) event.getPacket();

			if (packet.getLocation() == MessageType.GAME_INFO)
				return;

			Text message = packet.getMessage().shallowCopy();

			if (getSetting(6).asToggle().state) {
				message = Texts.forEachWord(message, (string, style) -> {
					String stripped = Formatting.strip(string);
					if (stripped.matches("['\u00a1-\u00f5]+\u00ff[0-~]+")) {
						String decrypted = decrypt(stripped);
						if (decrypted != null) {
							return new LiteralText("<").styled(s -> s.withColor(BleachHack.watermark.getColor1()))
									.append(new LiteralText(decrypted).styled(s -> s.withColor(0xffffff).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(string).setStyle(style)))))
									.append(new LiteralText(">").styled(s -> s.withColor(BleachHack.watermark.getColor2())));
						}
					}

					return null;
				});
			}

			if (!filterPatterns.isEmpty() && getSetting(4).asToggle().state) {
				int mode = getSetting(4).asToggle().getChild(0).asMode().mode;
				if (mode == 0) {
					for (Pattern pattern: filterPatterns) {
						message = Texts.replaceAll(message, pattern,
								(string, style) -> new LiteralText(StringUtils.repeat('|', string.length() * 2))
								.styled(s -> s
										.withColor(Formatting.GRAY)
										.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(string).setStyle(style)))));
					}
				} else {
					for (Pattern pat: filterPatterns) {
						if (pat.matcher(message.getString()).find()) {
							if (mode == 1) {
								Text messageCopy = message.shallowCopy();
								message = new LiteralText("\u00a77Blocked Message").styled(style -> style
										.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, messageCopy)));
								break;
							} else {
								event.setCancelled(true);
								return;
							}
						}
					}
				}
			}

			if (getSetting(3).asToggle().state) {
				DateTimeFormatter formatter = getSetting(3).asToggle().getChild(0).asToggle().state
						? DateTimeFormatter.ofPattern("HH:mm:ss") : DateTimeFormatter.ofPattern("HH:mm");
				message = new LiteralText("\u00a78[\u00a77" + LocalDateTime.now().format(formatter) + "\u00a78] ").append(message);
			}

			if (!message.equals(packet.getMessage())) {
				packet.message = message;
			}
		}
	}

	private String encrypt(String text, String key) {
		byte[] keyBytes = Hashing.sha512().hashUnencodedChars(key).asBytes();
		byte[] encrypted = new byte[text.getBytes(StandardCharsets.UTF_8).length + 1];

		int i = 0;
		for (byte b: ('~' + text).getBytes(StandardCharsets.UTF_8)) {
			if (i % keyBytes.length == 0 && i != 0)
				keyBytes = Hashing.sha512().hashBytes(keyBytes).asBytes();

			encrypted[i] = (byte) (b ^ keyBytes[i % keyBytes.length]);
			i++;
		}

		byte[] baseEncrypted = Ascii85.encode(encrypted).getBytes(StandardCharsets.ISO_8859_1);
		int cutoff = 2;
		while (baseEncrypted.length > 254 - key.length()) {
			baseEncrypted = Ascii85.encode(ArrayUtils.subarray(encrypted, 0, encrypted.length - cutoff)).getBytes(StandardCharsets.ISO_8859_1);
			cutoff += 2;
		}

		// Ascii85 so chunky we have to move it to block 2 to not cause any problems
		for (int j = 0; j < baseEncrypted.length; j++)
			if (baseEncrypted[j] != 39)
				baseEncrypted[j] += 128;

		return new String(baseEncrypted, StandardCharsets.ISO_8859_1);
	}

	private String decrypt(String text) {
		String[] split = text.split("\u00ff");
		if (split.length != 2)
			return null;

		try {
			byte[] baseEncrypted = split[0].getBytes(StandardCharsets.ISO_8859_1);
			for (int j = 0; j < baseEncrypted.length; j++) {
				if (baseEncrypted[j] != 39)
					baseEncrypted[j] -= 128;
			}

			byte[] encrypted = Ascii85.decode(new String(baseEncrypted, StandardCharsets.ISO_8859_1));
			byte[] keyBytes = Hashing.sha512().hashUnencodedChars(split[1]).asBytes();

			for (int i = 0; i < encrypted.length; i++) {
				if (i % keyBytes.length == 0 && i != 0)
					keyBytes = Hashing.sha512().hashBytes(keyBytes).asBytes();

				encrypted[i] = (byte) (encrypted[i] ^ keyBytes[i % keyBytes.length]);
			}

			String finalString = new String(encrypted, StandardCharsets.UTF_8);
			// Checksum
			if (finalString.charAt(0) == '~') {
				return finalString.substring(1);
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	private String getRandomString(int len) {
		String string = "";
		for (int i = 0; i < len; i++) {
			string += (char) ThreadLocalRandom.current().nextInt(48, 126);
		}

		return string;
	}

	static class CustomFont {

		private Map<Character, Character> allMaps = new HashMap<>();

		public CustomFont(CharMap... maps) {
			for (CharMap map : maps) {
				allMaps.putAll(map.getMap());
			}
		}

		public String replace(String startString) {
			for (Entry<Character, Character> e : allMaps.entrySet()) {
				startString = startString.replace(e.getKey(), e.getValue());
			}

			return startString;
		}

		static class CharMap {

			private Map<Character, Character> map = new HashMap<>();

			private CharMap(char... mappings) {
				for (int i = 0; i < mappings.length - 1; i += 2) {
					map.put(mappings[i], mappings[i + 1]);
				}
			}

			public static CharMap single(char from, char to) {
				return new CharMap(from, to);
			}

			public static CharMap range(char start, int start1, int amount) {
				char[] chars = new char[amount * 2];

				for (int i = 0; i < amount; i++) {
					chars[i * 2] = (char) (start + i);
					chars[i * 2 + 1] = (char) (start1 + i);
				}

				return new CharMap(chars);
			}

			public Map<Character, Character> getMap() {
				return map;
			}
		}
	}
}
