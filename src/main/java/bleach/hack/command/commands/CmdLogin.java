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
import bleach.hack.utils.LoginManager;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.LiteralText;

public class CmdLogin extends Command {

    @Override
    public String getAlias() {
        return "login";
    }

    @Override
    public String getDescription() {
        return "Login to an account";
    }

    @Override
    public String getSyntax() {
        return "login [email] [password]";
    }

    @Override
    public void onCommand(String command, String[] args) throws Exception {
        if (args[0] == null || args[1] == null) {
            BleachLogger.errorMessage("Missing email or password.");
            return;
        }
        String loginResult = LoginManager.login(args[0], args[1]);
        try {
                if (loginResult.equals("\u00a7aLogin Successful")) {
                    this.mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(new LiteralText("Successfully logged in as \"" + mc.getSession().getUsername() + "\", please reconnect.")));
                } else {
                    BleachLogger.errorMessage(loginResult);
                }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

}
