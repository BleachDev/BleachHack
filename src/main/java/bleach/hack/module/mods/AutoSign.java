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
package bleach.hack.module.mods;

import bleach.hack.event.events.EventOpenScreen;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.FabricReflect;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AutoSign extends Module {

    public String[] text = new String[]{};

    public AutoSign() {
        super("AutoSign", KEY_UNBOUND, Category.PLAYER, "Automatically writes on signs",
                new SettingToggle("Random", false));
    }

    public void onDisable() {
        text = new String[]{};
        super.onDisable();
    }

    @Subscribe
    public void sendPacket(EventSendPacket event) {
        if (event.getPacket() instanceof UpdateSignC2SPacket && text.length < 3) {
            text = ((UpdateSignC2SPacket) event.getPacket()).getText();
        }
    }

    @Subscribe
    public void onOpenScreen(EventOpenScreen event) {
        if (text.length < 3) return;

        if (event.getScreen() instanceof SignEditScreen) {
            event.setCancelled(true);

            if (getSetting(0).asToggle().state) {
                text = new String[]{};
                while (text.length < 4) {
                    IntStream chars = new Random().ints(0, 0x10FFFF);
                    text = chars.limit(1000).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining()).split("(?<=\\G.{250})");
                }
            }

            SignEditScreen screen = (SignEditScreen) event.getScreen();
            SignBlockEntity sign = (SignBlockEntity) FabricReflect.getFieldValue(screen, "field_3031", "sign");

            mc.player.networkHandler.sendPacket(new UpdateSignC2SPacket(sign.getPos(), text[0], text[1], text[2], text[3]));
        }
    }
}
