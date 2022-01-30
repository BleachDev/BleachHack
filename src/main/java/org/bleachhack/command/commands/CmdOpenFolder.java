/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.util.BleachLogger;

import java.awt.*;
import java.io.File;
import java.nio.file.Paths;
public class CmdOpenFolder extends Command {

    public CmdOpenFolder() {
        super("openfolder", "Opens DarkHack folder", "openfolder", CommandCategory.MODULES);
    }

    @Override
    public void onCommand(String command, String[] args) throws Exception {
        BleachLogger.info("Opening DarkHack folder");
        if (!GraphicsEnvironment.isHeadless()) {
            System.setProperty("java.awt.headless", "false");
        }
        Util.getOperatingSystem().open(new File(String.valueOf(Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "/darkhack/"))));
    }

}