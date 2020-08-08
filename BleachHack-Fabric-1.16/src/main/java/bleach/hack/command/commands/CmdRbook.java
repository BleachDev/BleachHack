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
package bleach.hack.command.commands;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.math.NumberUtils;

import bleach.hack.command.Command;
import bleach.hack.utils.BleachLogger;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.util.Hand;

public class CmdRbook extends Command {

	@Override
	public String getAlias() {
		return "rbook";
	}

	@Override
	public String getDescription() {
		return "Generates a random book";
	}

	@Override
	public String getSyntax() {
		return "rbook [pages] [start char] [end char] [chrs/page]";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		ItemStack item = mc.player.inventory.getMainHandStack();

		if (item.getItem() != Items.WRITABLE_BOOK) {
			BleachLogger.errorMessage("Not Holding A Writable Book!");
			return;
		}

		int pages = args.length >= 1 ? Math.min(NumberUtils.toInt(args[0], 100), 100) : 100;
		int startChar = args.length >= 2 ? NumberUtils.toInt(args[1]) : 0;
		int endChar = args.length >= 3 ? NumberUtils.toInt(args[2], 0x10FFFF) : 0x10FFFF;
		int pageChars = args.length >= 4 ? NumberUtils.toInt(args[3], 210) : 210;

		IntStream chars = new Random().ints(startChar, endChar + 1);
		String text = chars.limit(pageChars*100).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());

		ListTag textSplit = new ListTag();

		for (int t = 0; t < pages; t++) textSplit.add(StringTag.of(text.substring(t * pageChars, (t + 1) * pageChars)));

		item.getOrCreateTag().put("pages", textSplit);
		mc.player.networkHandler.sendPacket(new BookUpdateC2SPacket(item, false, Hand.MAIN_HAND));
	}

}
