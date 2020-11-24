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

public class CmdCredits extends Command {

    @Override
    public String getAlias() {
        return "credit";
    }

    @Override
    public String getDescription() {
        return "credits to people";
    }

    @Override
    public String getSyntax() {
        return "credit";
    }

    @Override
    public void onCommand(String command, String[] args) throws Exception {
        String cmd = null;
        try {
            cmd = args[0];
        } catch (Exception e) {
        }

        BleachLogger.noPrefixMessage("");
        BleachLogger.noPrefixMessage("\u00A79-=\u00A7f+\u00A79=- BleachHack epearl Edition Credits -=\u00A7f+\u00A79=-");
        BleachLogger.noPrefixMessage("\u00A79bleach \u00A7fmade the base and like 80% of the modules and also helped fix my dumb issues");
        BleachLogger.noPrefixMessage("\u00A79epearl \u00A7fmade this coolboy skid");
        BleachLogger.noPrefixMessage("\u00A79kami \u00A7fprobably stole modules from and forgot");
        BleachLogger.noPrefixMessage("\u00A79seppuku \u00A7fprobably stole modules from and forgot");
        BleachLogger.noPrefixMessage("\u00A79ethius \u00A7fcontributed stuff and is cool");
        BleachLogger.noPrefixMessage("\u00A79evan \u00A7fcontributed stuff and is cool");
        BleachLogger.noPrefixMessage("\u00A79axua \u00A7fcalled me a pepega and showed me code samples");
        BleachLogger.noPrefixMessage("\u00A79meteor ppl \u00A7ffanboy me for some reason and its weird");
        BleachLogger.noPrefixMessage("");
    }

}
