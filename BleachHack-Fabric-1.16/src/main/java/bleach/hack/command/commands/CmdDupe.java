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
package bleach.hack.command.commands;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import bleach.hack.command.Command;
import bleach.hack.util.BleachLogger;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.text.LiteralText;

public class CmdDupe extends Command {

	@Override
	public String getAlias() {
		return "dupe";
	}

	@Override
	public String getDescription() {
		return "Dupes items, (Vanilla mode patched on 1.14.4+)";
	}

	@Override
	public String getSyntax() {
		return "dupe <vanilla/book>";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if (args.length == 0) {
			printSyntaxError("Invaild dupe method");
			return;
		}

		if (args[0].equalsIgnoreCase("vanilla")) {
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
