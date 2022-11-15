/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventPacket;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;

import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

public class ColorSigns extends Module {

	public ColorSigns() {
		super("ColorSigns", KEY_UNBOUND, ModuleCategory.EXPLOITS, "Allows you to use colors on signs on NON-PAPER servers (use \"&\" for color symbols).");
	}

	/* This works because the code to strip invalid characters from signs is flawed
	 * because it uses a replaceAll for all the formatting codes instead of matching
	 * all sections symbols, which means you can basically "stack" two formatting
	 * codes ontop of eachother like "&&66", it will when search and find the middle
	 * one and remove it to leave "&6" left which is still a valid formatting code.
	 * Paper has a patch for it to correct it so it doesn't work there */
	@BleachSubscribe
	public void onPacketSend(EventPacket.Send event) {
		if (event.getPacket() instanceof UpdateSignC2SPacket) {
			UpdateSignC2SPacket p = (UpdateSignC2SPacket) event.getPacket();

			for (int l = 0; l < p.getText().length; l++) {
				String newText = p.getText()[l].replaceAll("(?i)\u00a7|&([0-9A-FK-OR])", "\u00a7\u00a7$1$1");
				p.getText()[l] = newText;
			}
		}
	}
}
