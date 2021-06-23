/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BleachLogger {

	public static final Logger logger = LogManager.getFormatterLogger("BleachHack");

	public static void infoMessage(MutableText t) {
		try {
			MinecraftClient.getInstance().inGameHud.getChatHud()
			.addMessage(new LiteralText(getBHText(Formatting.DARK_AQUA) + "\u00a73\u00a7lINFO: \u00a73")
					.append(t.formatted(Formatting.DARK_AQUA)));
		} catch (Exception e) {
			logger.log(Level.INFO, t.asString());
		}
	}

	public static void infoMessage(String s) {
		infoMessage(new LiteralText(s));
	}

	public static void warningMessage(MutableText t) {
		try {
			MinecraftClient.getInstance().inGameHud.getChatHud()
			.addMessage(new LiteralText(getBHText(Formatting.YELLOW) + "\u00a7e\u00a7lWARN: \u00a7e")
					.append(t.formatted(Formatting.YELLOW)));
		} catch (Exception e) {
			logger.log(Level.WARN, t.asString());
		}
	}

	public static void warningMessage(String s) {
		warningMessage(new LiteralText(s));
	}

	public static void errorMessage(MutableText t) {
		try {
			MinecraftClient.getInstance().inGameHud.getChatHud()
			.addMessage(new LiteralText(getBHText(Formatting.RED) + "\u00a7c\u00a7lERROR: \u00a7c")
					.append(t.formatted(Formatting.RED)));
		} catch (Exception e) {
			logger.log(Level.ERROR, t.asString());
		}
	}

	public static void errorMessage(String s) {
		errorMessage(new LiteralText(s));
	}

	public static void noPrefixMessage(String s) {
		try {
			MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(new LiteralText(s));
		} catch (Exception e) {
			logger.log(Level.INFO, s);
		}
	}

	public static void noPrefixMessage(Text text) {
		try {
			MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(text);
		} catch (Exception e) {
			logger.log(Level.INFO, text.getString());
		}
	}

	private static String getBHText(Formatting color) {
		return color + "\u00a7l[\u00a76B\u00a7dH" + color + "\u00a7l] ";
	}
}
