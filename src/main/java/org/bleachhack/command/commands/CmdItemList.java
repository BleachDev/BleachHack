/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.io.BleachFileMang;

public class CmdItemList extends Command {

    public CmdItemList() {
        super("itemlist", "Makes a txt file with all item IDs", "itemlist", CommandCategory.MISC,
                "itemids");
    }

    @Override
    public void onCommand(String command, String[] args) {
        boolean first = true;
        StringBuilder builder = new StringBuilder();
        for (Item item : Registry.ITEM) {
            if (!first) {
                builder.append(",");
                first = false;
            }
            builder.append(Registry.ITEM.getId(item).getPath());
            builder.append('\n');
        }

        if (BleachFileMang.fileExists("itemlist.txt")) {
            BleachFileMang.deleteFile("itemlist.txt");
        }
        BleachFileMang.createEmptyFile("itemlist.txt");
        BleachFileMang.appendFile("itemlist.txt", builder.toString());

        BleachLogger.info("Saved item list as: itemlist.txt");

    }
}