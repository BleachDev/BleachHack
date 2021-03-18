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

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import bleach.hack.command.Command;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.AutoSteal;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.file.BleachFileMang;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class CmdAutoSteal extends Command {

	@Override
	public String getAlias() {
		return "autosteal";
	}

	@Override
	public String getDescription() {
		return "Edit AutoSteal items";
	}

	@Override
	public String getSyntax() {
		return "autosteal add <item> | autosteal remove <item> | autosteal clear | autosteal list";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		BleachFileMang.createFile("autostealitems.txt");

		List<String> lines = BleachFileMang.readFileLines("autostealitems.txt");
		lines.removeIf(StringUtils::isBlank);

		if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
			AutoSteal autosteal = ModuleManager.getModule(AutoSteal.class);
			String item = (args[1].contains(":") ? "" : "minecraft:") + args[1].toLowerCase(Locale.ENGLISH);

			if (args[0].equalsIgnoreCase("add")) {
				if (Registry.ITEM.get(new Identifier(item)) == Items.AIR) {
					BleachLogger.errorMessage("Invalid Item: " + args[1]);
					return;
				} else if (lines.contains(item)) {
					BleachLogger.errorMessage("Item is already added!");
					return;
				}

				BleachFileMang.appendFile(item, "autostealitems.txt");

				if (autosteal.isToggled()) {
					autosteal.toggle();
					autosteal.toggle();
				}

				BleachLogger.infoMessage("Added Item: " + args[1]);

			} else {
				if (lines.contains(item)) {
					lines.remove(item);

					String s = "";
					for (String s1 : lines)
						s += s1 + "\n";

					BleachFileMang.createEmptyFile("autostealitems.txt");
					BleachFileMang.appendFile(s, "autostealitems.txt");

					if (autosteal.isToggled()) {
						autosteal.toggle();
						autosteal.toggle();
					}

					BleachLogger.infoMessage("Removed Item: " + args[1]);
				} else {
					BleachLogger.errorMessage("Item Not In List: " + args[1]);
				}
			}
		} else if (args[0].equalsIgnoreCase("clear")) {
			BleachFileMang.createEmptyFile("autostealitems.txt");
			BleachLogger.infoMessage("Cleared AutoSteal Items");
		} else if (args[0].equalsIgnoreCase("list")) {
			String s = "";
			for (String l : lines) {
				s += "\n\u00a7b" + l;
			}

			BleachLogger.infoMessage(s);
		} else {
			printSyntaxError();
		}
	}
}
