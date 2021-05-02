/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.command.commands;

import java.util.Arrays;
import java.util.Locale;

import com.google.gson.JsonArray;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.command.exception.CmdSyntaxException;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.NoRender;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.file.BleachFileHelper;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class CmdCustomSign extends Command {

	public CmdCustomSign() {
		super("customsign", "Sets the NoRender custom sign text.", "customsign <line1/line2/line3/line4/all> <text> | customsign list", CommandCategory.MODULES);
	}

	@Override
	public void onCommand(String alias, String[] args) {
		if (args.length == 0) {
			throw new CmdSyntaxException();
		}

		NoRender noRender = (NoRender) ModuleManager.getModule("NoRender");

		if (args[0].equalsIgnoreCase("list")) {
			String s = "Sign Text:";
			for (Text text: noRender.signText) {
				s += "\n\u00a77" + text.getString();
			}

			BleachLogger.infoMessage(s);
			return;
		}

		String arg = args[0].toLowerCase(Locale.ENGLISH);
		boolean all = arg.equals("all");

		Text text = new LiteralText(String.join(" ", Arrays.asList(args).subList(1, args.length)));

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
		BleachLogger.infoMessage("Changed sign text!");
	}

}
