/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2019 Bleach.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.util;

import org.apache.logging.log4j.Level;

import bleach.hack.BleachHack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BleachLogger {
	
	public static void infoMessage(MutableText t) {
		try {
			MinecraftClient.getInstance().inGameHud.getChatHud()
			.addMessage(new LiteralText(getBHText(Formatting.DARK_AQUA) + "\u00a73\u00a7lINFO: \u00a73")
					.append(t.formatted(Formatting.DARK_AQUA)));
		} catch (Exception e) {
			BleachHack.logger.log(Level.INFO, t.asString());
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
			BleachHack.logger.log(Level.WARN, t.asString());
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
			BleachHack.logger.log(Level.ERROR, t.asString());
		}
	}
	
	public static void errorMessage(String s) {
		errorMessage(new LiteralText(s));
	}

	public static void noPrefixMessage(String s) {
		try {
			MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(new LiteralText(s));
		} catch (Exception e) {
			BleachHack.logger.log(Level.INFO, s);
		}
	}

	public static void noPrefixMessage(Text text) {
		try {
			MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(text);
		} catch (Exception e) {
			BleachHack.logger.log(Level.INFO, text.getString());
		}
	}

	private static String getBHText(Formatting color) {
		return color + "\u00a7l[\u00a76B\u00a7dH" + color + "\u00a7l] ";
	}
}
