/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command.commands;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.util.BleachLogger;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.text.LiteralText;

public class CmdDupe extends Command {

	public CmdDupe() {
		super("dupe", "Dupes items, (Old aka the 11/11 dupe is patched on 1.14.4+, Book mode works on all vanilla servers)", "dupe <old/book>", CommandCategory.MISC);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length == 0) {
			printSyntaxError("Invaild dupe method");
			return;
		}

		if (args[0].equalsIgnoreCase("old")) {
			mc.player.dropSelectedItem(true);
			mc.player.networkHandler.getConnection().disconnect(new LiteralText("Duping..."));
		} else if (args[0].equalsIgnoreCase("book")) {
			ItemStack item = mc.player.inventory.getMainHandStack();

			if (item.getItem() != Items.WRITABLE_BOOK) {
				BleachLogger.errorMessage("Not holding a writable book!");
				return;
			}

			IntStream chars = new Random().ints(0, 0x10ffff);
			String text = chars.limit(25000 * 50).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());

			ListTag textSplit = new ListTag();

			for (int t = 0; t < 50; t++)
				textSplit.add(StringTag.of(text.substring(t * 25000, (t + 1) * 25000)));

			item.getOrCreateTag().put("pages", textSplit);
			mc.player.networkHandler.sendPacket(new BookUpdateC2SPacket(item, false, mc.player.inventory.selectedSlot));
		} else {
			printSyntaxError("Invaild dupe method");
		}
	}

}
