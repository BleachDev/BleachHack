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

import bleach.hack.command.Command;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.file.BleachJsonHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class CmdNBT extends Command {

	@Override
	public String getAlias() {
		return "nbt";
	}

	@Override
	public String getDescription() {
		return "NBT stuff";
	}

	@Override
	public String getSyntax() {
		return "nbt get | nbt copy | nbt set <nbt> | nbt wipe";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if (args.length == 0) {
			printSyntaxError();
			return;
		}

		ItemStack item = mc.player.inventory.getMainHandStack();

		if (args[0].equalsIgnoreCase("get")) {
			CompoundTag tag = item.getTag();
			
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
			item.setTag(new CompoundTag());
		}

	}

}
