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
import bleach.hack.util.file.BleachJsonHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class CmdNBT extends Command {

	public CmdNBT() {
		super("nbt", "NBT stuff.", "nbt get | nbt copy | nbt set <nbt> | nbt wipe", CommandCategory.MISC);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (args.length == 0) {
			printSyntaxError();
			return;
		}

		ItemStack item = mc.player.getInventory().getMainHandStack();

		if (args[0].equalsIgnoreCase("get")) {
			NbtCompound tag = item.getTag();

			if (tag == null) {
				BleachLogger.infoMessage("\u00a7c\u00a7lNo NBT on this item!");
				return;
			}

			String stringTag = BleachJsonHelper.formatJson(tag.toString());

			Text copy = new LiteralText("\u00a7e\u00a7l<COPY>")
					.styled(s ->
					s.withClickEvent(
							new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, stringTag))
					.withHoverEvent(
							new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Copy the nbt of this item to your clipboard"))));

			BleachLogger.infoMessage(new LiteralText("\u00a76\u00a7lNBT: ").append(copy).append("\u00a76\n" + stringTag));
		} else if (args[0].equalsIgnoreCase("copy")) {
			mc.keyboard.setClipboard(item.getTag() + "");
			BleachLogger.infoMessage("\u00a76Copied\n\u00a7f" + (item.getTag() + "\n") + "\u00a76to clipboard.");
		} else if (args[0].equalsIgnoreCase("set")) {
			try {
				if (args[1].isEmpty()) {
					printSyntaxError();
					return;
				}

				item.setTag(StringNbtReader.parse(args[1]));
				BleachLogger.infoMessage("\u00a76Set NBT of " + item.getItem().getName() + "to\n\u00a7f" + (item.getTag()));
			} catch (Exception e) {
				printSyntaxError();
			}
		} else if (args[0].equalsIgnoreCase("wipe")) {
			item.setTag(new NbtCompound());
		}

	}

}
