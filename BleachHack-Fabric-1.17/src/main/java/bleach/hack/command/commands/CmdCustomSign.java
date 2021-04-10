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

import java.util.Arrays;
import java.util.Locale;

import com.google.gson.JsonArray;

import bleach.hack.command.Command;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.NoRender;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.file.BleachFileHelper;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class CmdCustomSign extends Command {

	@Override
	public String getAlias() {
		return "customsign";
	}

	@Override
	public String getDescription() {
		return "Sets the NoRender custom sign text";
	}

	@Override
	public String getSyntax() {
		return "customsign <line1/line2/line3/line4/all> <text> | customsign list";
	}

	@Override
	public void onCommand(String command, String[] args) {
		if (args.length == 0) {
			printSyntaxError();
			return;
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
