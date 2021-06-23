/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import bleach.hack.eventbus.BleachSubscribe;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleCategory;
import bleach.hack.util.PlayerInteractEntityC2SUtils;
import bleach.hack.util.PlayerInteractEntityC2SUtils.InteractType;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class MountBypass extends Module {

	public boolean dontCancel = false;

	public MountBypass() {
		super("MountBypass", KEY_UNBOUND, ModuleCategory.EXPLOITS, "Bypasses illegalstack on non bungeecord servers");
	}

	@BleachSubscribe
	public void onPacket(EventSendPacket event) {
		if (dontCancel)
			return;

		if (event.getPacket() instanceof PlayerInteractEntityC2SPacket
				&& PlayerInteractEntityC2SUtils.getEntity(
						(PlayerInteractEntityC2SPacket) event.getPacket()) instanceof AbstractDonkeyEntity
				&& PlayerInteractEntityC2SUtils.getInteractType(
						(PlayerInteractEntityC2SPacket) event.getPacket()) == InteractType.INTERACT_AT) {
			event.setCancelled(true);
		}
	}
}
