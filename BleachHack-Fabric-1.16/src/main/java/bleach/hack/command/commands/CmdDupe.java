/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.util.BleachLogger;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;

public class CmdDupe extends Command {

	public CmdDupe() {
		super("dupe", "Dupes items on VANILLA servers using the book dupe", "dupe", CommandCategory.MISC);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (mc.player.inventory.getMainHandStack().getItem() != Items.WRITABLE_BOOK) {
			BleachLogger.errorMessage("Not holding a writable book!");
			return;
		}

		NbtList listTag = new NbtList();

		StringBuilder builder1 = new StringBuilder();
		for(int i = 0; i < 21845; i++)
			builder1.append((char) 2077);

		listTag.add(0, NbtString.of(builder1.toString()));

		StringBuilder builder2 = new StringBuilder();
		for(int i = 0; i < 32; i++)
			builder2.append("BleachHK");

		String string2 = builder2.toString();
		for(int i = 1; i < 40; i++)
			listTag.add(i, NbtString.of(string2));

		ItemStack bookStack = new ItemStack(Items.WRITABLE_BOOK, 1);
		bookStack.putSubTag("title", NbtString.of("If you can see this, it didn't work"));
		bookStack.putSubTag("pages", listTag);

		mc.player.networkHandler.sendPacket(new BookUpdateC2SPacket(bookStack, true, mc.player.inventory.selectedSlot));
	}

}
