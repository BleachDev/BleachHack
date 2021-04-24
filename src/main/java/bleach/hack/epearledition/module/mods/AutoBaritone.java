///*
// * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
// * Copyright (c) 2019 Bleach.
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//package bleach.hack.module.mods;
//
//import baritone.api.event.events.ChatEvent;
//import bleach.hack.event.Event;
//import bleach.hack.event.events.EventReadPacket;
//import bleach.hack.event.events.EventSendPacket;
//import bleach.hack.event.events.EventTick;
//import bleach.hack.module.Category;
//import bleach.hack.module.Module;
//import bleach.hack.setting.base.SettingMode;
//import bleach.hack.setting.base.SettingSlider;
//import bleach.hack.utils.BleachLogger;
//import bleach.hack.utils.file.BleachFileMang;
//import com.google.common.eventbus.Subscribe;
//import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//public class AutoBaritone extends Module {
//
//    private final Random rand = new Random();
//    private List<String> lines = new ArrayList<>();
//    private int lineCount = 0;
//
//    public AutoBaritone() {
//        super("AutoBaritone", KEY_UNBOUND, Category.MISC, "fortnite",
//                new SettingSlider("Delay", 1, 600, 20, 0));
//    }
//
//    @Override
//    public void onEnable() {
//        super.onEnable();
//        BleachFileMang.createFile("baritone.txt");
//        lines = BleachFileMang.readFileLines("baritone.txt");
//        lineCount = 0;
//    }
//
//    @Subscribe
//    public void onTick(EventTick event) {
//        if (lines.isEmpty()) return;
//
//        if (mc.player.age % (int) (getSetting(0).asSlider().getValue() * 20) == 0) {
//            mc.player.sendChatMessage(lines.get(lineCount));
//
//            if (lineCount >= lines.size() - 1) lineCount = 0;
//            else lineCount++;
//        }
//    }
//
//    @Subscribe
//    public void onPacketRead(Event event) {
//            BleachLogger.infoMessage("TEST: " + event.toString());
//    }
//}
