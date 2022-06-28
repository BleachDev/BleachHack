/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import org.bleachhack.event.events.EventPacket;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.util.io.BleachFileMang;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Greeter extends Module {

    private final Random rand = new Random();
    private List<String> lines = new ArrayList<>();
    private int lineCount = 0;
    public List<String> message_queue = new ArrayList<>();
    public String player;
    public String message;


    public Greeter() {
        super("Greeter", KEY_UNBOUND, ModuleCategory.MISC, "Welcomes players (edit in greeter.txt)",
                new SettingMode("Read", "Order", "Random"),
                new SettingSlider("Delay", 0, 20, 3, 0).withDesc("Second delay between messages to avoid spam kicks"));
    }

    @BleachSubscribe
    public void onTick(EventTick event) {
        if (mc.player.age % (this.getSettings().get(1).asSlider().getValue() * 20) == 0 && this.isEnabled()) {
            if (message_queue.size() > 0) {
                message = message_queue.get(0);
                mc.player.sendChatMessage(message);
                message_queue.remove(0);
            }
        }
    }

    @Override
    public void onEnable(boolean inWorld) {
        super.onEnable(inWorld);
        if (!BleachFileMang.fileExists("greeter.txt")) {
            BleachFileMang.createFile("greeter.txt");
            BleachFileMang.appendFile("greeter.txt", "Welcome, $p");

        }

        lines = BleachFileMang.readFileLines("greeter.txt");
        lineCount = 0;
    }

    @BleachSubscribe
    public void onPacketRead(EventPacket event) {
        if ((event.getPacket() instanceof PlayerListS2CPacket) && (((PlayerListS2CPacket) event.getPacket()).getAction() == PlayerListS2CPacket.Action.ADD_PLAYER)) {
            player = ((PlayerListS2CPacket) event.getPacket()).getEntries().get(0).getProfile().getName();
            if (lines.isEmpty()) return;
            if (player == null) return;
            if (mc.player == null) return;
            if (player.equals(mc.player.getDisplayName().toString())) return;
            if (getSetting(0).asMode().getMode() == 0) {
                message_queue.add(lines.get(lineCount).replace("$p", player));
            } else if (getSetting(0).asMode().getMode() == 1) {
                message_queue.add(lines.get(rand.nextInt(lines.size())).replace("$p", player));
            }
            if (lineCount >= lines.size() - 1) lineCount = 0;
            else lineCount++;
            player = null;
        }
    }
}
