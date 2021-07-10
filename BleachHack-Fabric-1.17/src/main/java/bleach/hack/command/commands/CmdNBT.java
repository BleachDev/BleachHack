/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command.commands;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.command.exception.CmdSyntaxException;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.io.BleachJsonHelper;
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
			throw new CmdSyntaxException();
		}

		ItemStack item = mc.player.getInventory().getMainHandStack();

		if (args[0].equalsIgnoreCase("get")) {
			NbtCompound tag = item.getNbt();

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
			mc.keyboard.setClipboard(item.getNbt() + "");
			BleachLogger.infoMessage("\u00a76Copied\n\u00a7f" + item.getNbt() + "\n\u00a76to clipboard.");
		} else if (args[0].equalsIgnoreCase("set")) {
			try {
				if (args.length < 2) {
					throw new CmdSyntaxException();
				}

				item.setNbt(StringNbtReader.parse(StringUtils.join(ArrayUtils.subarray(args, 1, args.length), ' ')));
				BleachLogger.infoMessage("\u00a76Set NBT of " + item.getItem().getName().getString() + " to\n" + BleachJsonHelper.formatJson(item.getNbt().toString()));
			} catch (Exception e) {
				throw new CmdSyntaxException();
			}
		} else if (args[0].equalsIgnoreCase("wipe")) {
			item.setNbt(new NbtCompound());
		}

	}

}
