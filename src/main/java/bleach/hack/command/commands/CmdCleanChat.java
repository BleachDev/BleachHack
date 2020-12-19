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
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.file.BleachFileMang;

import java.util.List;

public class CmdCleanChat extends Command {

    @Override
    public String getAlias() {
        return "cleanchat";
    }

    @Override
    public String getDescription() {
        return "remove or add a word to the cleanchat blacklist";
    }

    @Override
    public String getSyntax() {
        return "cleanchat add/del [word]";
    }

    @Override
    public void onCommand(String command, String[] args) throws Exception {
            if (args[0] == null) {
                BleachLogger.errorMessage("Expected add or del.");
                return;
            }
            if (args[0].toLowerCase().contains("add")) {
                BleachFileMang.appendFile(args[1].toLowerCase(), "cleanchat.txt");
                BleachLogger.infoMessage("Word \"" + args[1] + "\" has been added to the list of blacklisted words");
            } else if (args[0].toLowerCase().contains("del")) {
                List<String> lines = BleachFileMang.readFileLines("cleanchat.txt");
                lines.removeIf(s -> s.equals(args[1].toLowerCase()));
                BleachFileMang.createEmptyFile("cleanchat.txt");
                for (String line : lines) {
                    BleachFileMang.appendFile(line.toLowerCase(), "cleanchat.txt");
                }
                BleachLogger.infoMessage("Word \"" + args[1] + "\" has been removed from the list of blacklisted words");
            }
    }

}
