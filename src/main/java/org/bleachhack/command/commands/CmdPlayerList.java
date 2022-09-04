/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import net.minecraft.client.network.PlayerListEntry;
import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.io.BleachFileMang;

public class CmdPlayerList extends Command {

    public CmdPlayerList() {
        super("playerlist", "Makes a txt file with all players", "playerlist", CommandCategory.MISC,
                "players");
    }

    @Override
    public void onCommand(String command, String[] args) {
        StringBuilder builder = new StringBuilder();
        for (PlayerListEntry player : mc.player.networkHandler.getPlayerList()) {
            builder.append(player.getProfile().getName());
            builder.append('\n');
        }

        if (BleachFileMang.fileExists("playerlist.txt")) {
            BleachFileMang.deleteFile("playerlist.txt");
        }
        BleachFileMang.createEmptyFile("playerlist.txt");
        BleachFileMang.appendFile("playerlist.txt", builder.toString());

        BleachLogger.info("Saved player list as: playerlist.txt");

    }
}