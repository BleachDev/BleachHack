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

import bleach.hack.command.Command;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Xray;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.file.BleachFileMang;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class CmdXray extends Command {
    //brb stealing your nuker command
    @Override
    public String getAlias() {
        return "xray";
    }

    @Override
    public String getDescription() {
        return "Edit Xray blocks";
    }

    @Override
    public String getSyntax() {
        return "xray add [block] | xray remove [block] | xray clear | xray list";
    }

    @Override
    public void onCommand(String command, String[] args) throws Exception {
        BleachFileMang.createFile("xrayblocks.txt");

        List<String> lines = BleachFileMang.readFileLines("xrayblocks.txt");
        lines.removeIf(s -> s.isEmpty());
        System.out.println(lines);

        if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
            Xray xray = (Xray) ModuleManager.getModule(Xray.class);
            String block = (args[1].contains(":") ? "" : "minecraft:") + args[1].toLowerCase();

            if (args[0].equalsIgnoreCase("add")) {
                if (Registry.BLOCK.get(new Identifier(block)) == Blocks.AIR) {
                    BleachLogger.errorMessage("Invalid Block: " + args[1]);
                    return;
                } else if (lines.contains(block)) {
                    BleachLogger.errorMessage("Block is already added!");
                    return;
                }

                BleachFileMang.appendFile(block, "xrayblocks.txt");

                if (xray.isToggled()) {
                    xray.toggle();
                    xray.toggle();
                }

                BleachLogger.infoMessage("Added Block: " + args[1]);

            } else if (args[0].equalsIgnoreCase("remove")) {
                if (lines.contains(block)) {
                    lines.remove(block);

                    String s = "";
                    for (String s1 : lines) s += s1 + "\n";

                    BleachFileMang.createEmptyFile("xrayblocks.txt");
                    BleachFileMang.appendFile(s, "xrayblocks.txt");

                    if (xray.isToggled()) {
                        xray.toggle();
                        xray.toggle();
                    }

                    BleachLogger.infoMessage("Removed Block: " + args[1]);
                } else {
                    BleachLogger.errorMessage("Block Not In List: " + args[1]);
                }
            }
        } else if (args[0].equalsIgnoreCase("clear")) {
            BleachFileMang.createEmptyFile("xrayblocks.txt");
            BleachLogger.infoMessage("Cleared Xray Blocks");
        } else if (args[0].equalsIgnoreCase("list")) {
            String s = "";
            for (String l : lines) {
                s += "\n\u00a76" + l;
            }

            BleachLogger.infoMessage(s);
        }
    }
}

