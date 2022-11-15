/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import java.util.Arrays;
import java.util.Locale;

import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.command.exception.CmdSyntaxException;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.NoRender;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.io.BleachFileHelper;

import com.google.gson.JsonArray;

import net.minecraft.text.Text;

public class CmdCustomSign extends Command {

	public CmdCustomSign() {
		super("customsign", "Sets the NoRender custom sign text.", "customsign [line1/line2/line3/line4/all] <text> | customsign list", CommandCategory.MODULES);
	}

	@Override
	public void onCommand(String alias, String[] args) {
		if (args.length == 0) {
			throw new CmdSyntaxException();
		}

		NoRender noRender = ModuleManager.getModule(NoRender.class);

		if (args[0].equalsIgnoreCase("list")) {
			String s = "Sign Text:";
			for (Text text: noRender.signText) {
				s += "\n\u00a77" + text.getString();
			}

			BleachLogger.info(s);
			return;
		}

		String arg = args[0].toLowerCase(Locale.ENGLISH);
		boolean all = arg.equals("all");

		Text text = Text.literal(String.join(" ", Arrays.asList(args).subList(1, args.length)));

		boolean[] linesToChange = new boolean[] {
				arg.equals("line1") || all,
				arg.equals("line2") || all,
				arg.equals("line3") || all,
				arg.equals("line4") || all };

		JsonArray json = new JsonArray();
		for (int i = 0; i < 4; i++) {
			if (linesToChange[i]) {
				noRender.signText[i] = text;
				json.add(noRender.signText[i].getString());
			}
		}

		BleachFileHelper.saveMiscSetting("customSignText", json);
		BleachLogger.info("Changed sign text!");
	}

}
