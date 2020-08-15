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
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Nuker;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.file.BleachFileMang;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;

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
        BleachFileMang.createFile("nukerblocks.txt");

        List<String> lines = BleachFileMang.readFileLines("nukerblocks.txt");
        lines.removeIf(s -> s.isEmpty());

        if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
            Module mod = ModuleManager.getModule(Nuker.class);
            String block = (args[1].contains(":") ? "" : "minecraft:") + args[1].toLowerCase();

            if (args[0].equalsIgnoreCase("add")) {
                if (Registry.BLOCK.get(new Identifier(block)) == Blocks.AIR) {
                    BleachLogger.errorMessage("Invalid Block: " + args[1]);
                    return;
                } else if (lines.contains(block)) {
                    BleachLogger.errorMessage("Block is already added!");
                    return;
                }

                BleachFileMang.appendFile(block, "nukerblocks.txt");

                if (mod.isToggled()) {
                    mod.toggle();
                    mod.toggle();
                }

                BleachLogger.infoMessage("Added Block: " + block);

            } else if (args[0].equalsIgnoreCase("remove")) {
                if (lines.contains(block)) {
                    lines.remove(block);

                    String s = "";
                    for (String s1 : lines) s += s1 + "\n";

                    BleachFileMang.createEmptyFile("nukerblocks.txt");
                    BleachFileMang.appendFile(s, "nukerblocks.txt");

                    if (mod.isToggled()) {
                        mod.toggle();
                        mod.toggle();
                    }

                    BleachLogger.infoMessage("Removed Block: " + block);
                } else {
                    BleachLogger.errorMessage("Block Not In List: " + block);
                }
            }
        } else if (args[0].equalsIgnoreCase("clear")) {
            BleachFileMang.createEmptyFile("nukerblocks.txt");
            BleachLogger.infoMessage("Cleared Nuker Blocks");
        } else if (args[0].equalsIgnoreCase("list")) {
            String s = "";
            for (String l : lines) {
                s += "\n\u00a76" + l;
            }

            BleachLogger.infoMessage(s);
        }
    }

}
