/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bleachhack.BleachHack;

import net.minecraft.client.MinecraftClient;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BleachLogger {

	public static final Logger logger = LogManager.getFormatterLogger("BleachHack");

	public static int INFO_COLOR = 0x64b9fa;
	public static int WARN_COLOR = Formatting.YELLOW.getColorValue();
	public static int ERROR_COLOR = Formatting.RED.getColorValue();
	
	// Info
	
	public static void info(String s) {
		info(Text.literal(s));
	}

	public static void info(Text t) {
		try {
			MinecraftClient.getInstance().inGameHud.getChatHud()
			.addMessage(getBHText(INFO_COLOR)
					//.append("\u00a73\u00a7lINFO: \u00a73")
					.append(((MutableText) t).styled(s -> s.withColor(INFO_COLOR))));
		} catch (Exception e) {
			logger.log(Level.INFO, t.getString());
		}
	}
	
	// Warn
	
	public static void warn(String s) {
		warn(Text.literal(s));
	}

	public static void warn(Text t) {
		try {
			MinecraftClient.getInstance().inGameHud.getChatHud()
			.addMessage(getBHText(WARN_COLOR)
					//.append("\u00a7e\u00a7lWARN: \u00a7e")
					.append(((MutableText) t).styled(s -> s.withColor(WARN_COLOR))));
		} catch (Exception e) {
			logger.log(Level.WARN, t.getString());
		}
	}
	
	// Error
	
	public static void error(String s) {
		error(Text.literal(s));
	}

	public static void error(Text t) {
		try {
			MinecraftClient.getInstance().inGameHud.getChatHud()
			.addMessage(getBHText(ERROR_COLOR)
					//.append("\u00a7c\u00a7lERROR: \u00a7c")
					.append(((MutableText) t).styled(s -> s.withColor(ERROR_COLOR))));
		} catch (Exception e) {
			logger.log(Level.ERROR, t.getString());
		}
	}

	public static void noPrefix(String s) {
		noPrefix(Text.literal(s));
	}

	public static void noPrefix(Text text) {
		try {
			MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(text);
		} catch (Exception e) {
			logger.log(Level.INFO, text.getString());
		}
	}

	private static MutableText getBHText(int color) {
		return Text.literal("[").styled(s -> s.withColor(color))
				.append(BleachHack.watermark.getText())
				.append(Text.literal("] ").styled(s -> s.withColor(color)));
	}
}
