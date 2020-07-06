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

import bleach.hack.command.Command;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Nuker;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.file.BleachFileMang;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class CmdNuker extends Command {

	@Override
	public String getAlias() {
		return "nuker";
	}

	@Override
	public String getDescription() {
		return "Edit Nuker Blocks";
	}

	@Override
	public String getSyntax() {
		return "nuker add [block] | nuker remove [block] | nuker clear | nuker list";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if (args[0].equalsIgnoreCase("add")) {
			if (Registry.BLOCK.get(new Identifier(args[1].toLowerCase())) == Blocks.AIR) {
				BleachLogger.errorMessage("Invalid Block: " + args[1]);
				return;
			}
			
			BleachFileMang.appendFile(args[1].toLowerCase(), "nukerblocks.txt");
			ModuleManager.getModule(Nuker.class).toggle();
			ModuleManager.getModule(Nuker.class).toggle();
			BleachLogger.infoMessage("Added Block: " + args[1]);
			
		} else if (args[0].equalsIgnoreCase("remove")) {
			List<String> lines = BleachFileMang.readFileLines("nukerblocks.txt");
			
			if (lines.contains(args[1].toLowerCase())) {
				lines.remove(args[1].toLowerCase());
				
				String s = "";
				for (String s1: lines) s += s1 + "\n";
				
				BleachFileMang.createEmptyFile("nukerblocks.txt");
				BleachFileMang.appendFile(s, "nukerblocks.txt");
				
				BleachLogger.infoMessage("Removed Block: " + args[1]);
			}
			
			BleachLogger.errorMessage("Block Not In List: " + args[1]);
		} else if (args[0].equalsIgnoreCase("clear")) {
			BleachFileMang.createEmptyFile("nukerblocks.txt");
			BleachLogger.infoMessage("Cleared Nuker Blocks");
		} else if (args[0].equalsIgnoreCase("list")) {
			List<String> lines = BleachFileMang.readFileLines("nukerblocks.txt");
			
			String s = "";
			for (String l: lines) {
				s += "§6" + l + "\n";
			}
			
			BleachLogger.infoMessage(s);
		}
	}

}
