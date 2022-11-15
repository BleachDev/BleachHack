/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.util.BleachLogger;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;

public class CmdRbook extends Command {

	public CmdRbook() {
		super("rbook", "Generates a random book.", "rbook <pages> <start char> <end char> <chrs/page>", CommandCategory.MISC,
				"randombook", "book");
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		ItemStack item = mc.player.getInventory().getMainHandStack();

		if (item.getItem() != Items.WRITABLE_BOOK) {
			BleachLogger.error("Not Holding A Writable Book!");
			return;
		}

		int pages = args.length >= 1 && NumberUtils.isCreatable(args[0]) ? NumberUtils.createNumber(args[0]).intValue() : 100;
		int startChar = args.length >= 2 && NumberUtils.isCreatable(args[1]) ? NumberUtils.createNumber(args[1]).intValue() : 0;
		int endChar = args.length >= 3 && NumberUtils.isCreatable(args[2]) ? NumberUtils.createNumber(args[2]).intValue() : 0x10FFFF;
		int pageChars = args.length >= 4 && NumberUtils.isCreatable(args[3]) ? NumberUtils.createNumber(args[3]).intValue() : 210;

		List<String> textSplit = new ArrayList<>();

		for (int t = 0; t < pages; t++)
			textSplit.add(RandomStringUtils.random(pageChars, startChar, endChar, false, false));

		mc.player.networkHandler.sendPacket(new BookUpdateC2SPacket(mc.player.getInventory().selectedSlot, textSplit, Optional.empty()));

		BleachLogger.info("Written book (" + pages + " pages, " + pageChars + " chars/page)");
	}

}
