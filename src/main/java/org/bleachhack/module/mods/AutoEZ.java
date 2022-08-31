/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.bleachhack.event.events.EventPacket;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.util.io.BleachFileMang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AutoEZ extends Module {
    private Random rand = new Random();
    private List<String> lines = new ArrayList<>();
    private int lineCount = 0;

    public AutoEZ() {
        super("AutoEZ", KEY_UNBOUND, ModuleCategory.MISC, "Sends a message when you kill someone (edit in autoez.txt)",
                new SettingMode("Message", "EZ", "Custom", "GG").withDesc("Send a chat message when you kill someone"),
                new SettingMode("Read", "Random", "Order").withDesc("How to read the custom ezmessage"));
    }

    @Override
    public void onEnable(boolean inWorld) {
        super.onEnable(inWorld);
        if (!BleachFileMang.fileExists("autoez.txt")) {
            BleachFileMang.createFile("autoez.txt");
            BleachFileMang.appendFile("autoez.txt", "$p Just got EZed with the muscles of DarkHack");
        }
        lines = BleachFileMang.readFileLines("autoez.txt");
        lineCount = 0;
    }


    @BleachSubscribe
    public void onPacketRead(EventPacket event) {
        if (event.getPacket() instanceof GameMessageS2CPacket) {
            String msg = ((GameMessageS2CPacket) event.getPacket()).content().getString();
            if (msg.contains(mc.player.getName().getString()) && msg.contains("by")) {
                for (PlayerEntity e : mc.world.getPlayers()) {
                    if (e == mc.player)
                        continue;
                    List<String> list = new ArrayList<>(Arrays.asList(msg.split(" ")));
                    int index = list.indexOf("by");

                    if (mc.player.distanceTo(e) < 12 && msg.contains(e.getName().getString())
                            && !msg.contains("<" + e.getName().getString() + ">") && !msg.contains("<" + mc.player.getName().getString() + ">") && (list.get(index + 1).equals(mc.player.getName().getString()))) {
                        if (getSetting(0).asMode().getMode() == 0) {
                            mc.player.sendChatMessage(e.getName().getString() + " just got EZed with the muscles of DarkHack", null);
                        } else if (getSetting(0).asMode().getMode() == 2) {
                            mc.player.sendChatMessage("GG, " + e.getName().getString() + ", but DarkHack is ontop!", null);
                        } else if (getSetting(0).asMode().getMode() == 1) {
                            if (getSetting(1).asMode().getMode() == 0) {
                                mc.player.sendChatMessage(lines.get(rand.nextInt(lines.size())).replace("$p", e.getName().getString()), null);
                            } else if (getSetting(1).asMode().getMode() == 1) {
                                mc.player.sendChatMessage(lines.get(lineCount).replace("$p", e.getName().getString()), null);
                            }

                            if (lineCount >= lines.size() - 1) {
                                lineCount = 0;
                            } else {
                                lineCount++;
                            }
                        }
                    }
                }
            }
        }
    }
}
