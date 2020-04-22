/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
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
package bleach.hack.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;

public class BleachLogger {

	public static void infoMessage(String s) {
		Minecraft.getInstance().ingameGUI.getChatGUI()
			.printChatMessage(new StringTextComponent("§5[BleachHack] §9§lINFO: §9" + s));
	}
	
	public static void warningMessage(String s) {
		Minecraft.getInstance().ingameGUI.getChatGUI()
			.printChatMessage(new StringTextComponent("§5[BleachHack] §e§lWARNING: §e" + s));
	}
	
	public static void errorMessage(String s) {
		Minecraft.getInstance().ingameGUI.getChatGUI()
			.printChatMessage(new StringTextComponent("§5[BleachHack] §c§lERROR: §c" + s));
	}
	
	public static void noPrefixMessage(String s) {
		Minecraft.getInstance().ingameGUI.getChatGUI()
			.printChatMessage(new StringTextComponent(s));
	}
}
