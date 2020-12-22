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
package bleach.hack;

import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.utils.FriendManager;
import bleach.hack.utils.Rainbow;
import bleach.hack.utils.file.BleachFileHelper;
import bleach.hack.utils.file.BleachFileMang;
import com.google.common.eventbus.EventBus;
import net.fabricmc.api.ModInitializer;

public class BleachHack implements ModInitializer {

    public static final String VERSION = "b2500";
    public static final int INTVERSION = 24;

    public static EventBus eventBus = new EventBus();

    public static FriendManager friendMang;

    @Override
    public void onInitialize() {
        //System.out.println("Detected JRE: " + System.getProperty("java.runtime.version"));
        //System.out.println("Pre headless toggle property: " + System.getProperty("java.awt.headless"));
        //System.setProperty("java.awt.headless", "false");
        //System.out.println("Post headless toggle property: " + System.getProperty("java.awt.headless"));
        BleachFileMang.init();
        BleachFileHelper.readModules();

        ClickGui.clickGui.initWindows();
        BleachFileHelper.readClickGui();
        BleachFileHelper.readPrefix();
        BleachFileHelper.readFriends();

        for (Module m : ModuleManager.getModules()) m.init();

        eventBus.register(new Rainbow());

        eventBus.register(new ModuleManager());


        if (!BleachFileMang.fileExists("drawn.txt")) {
            BleachFileMang.createFile("drawn.txt");
        }
        for (String s : BleachFileMang.readFileLines("drawn.txt")) {
            for (Module m : ModuleManager.getModules()) {
                if (m.getName().toLowerCase().equals(s.toLowerCase())) {
                    m.setDrawn(false);
                }
            }
        }
        if (!BleachFileMang.fileExists("cleanchat.txt")) {
            BleachFileMang.createFile("cleanchat.txt");
            BleachFileMang.appendFile("nigger", "cleanchat.txt");
            BleachFileMang.appendFile("fag", "cleanchat.txt");
            BleachFileMang.appendFile("discord.gg", "cleanchat.txt");
            BleachFileMang.appendFile("retard", "cleanchat.txt");
            BleachFileMang.appendFile("autism", "cleanchat.txt");
            BleachFileMang.appendFile("chink", "cleanchat.txt");
            BleachFileMang.appendFile("tranny", "cleanchat.txt");
            BleachFileMang.appendFile("fuck", "cleanchat.txt");
            BleachFileMang.appendFile("shit", "cleanchat.txt");
            BleachFileMang.appendFile("nigga", "cleanchat.txt");

        }
    }
}
