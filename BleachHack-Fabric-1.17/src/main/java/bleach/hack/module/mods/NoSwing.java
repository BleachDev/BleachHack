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
import bleach.hack.event.events.EventSwingHand;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingToggle;
import bleach.hack.module.Module;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;

public class NoSwing extends Module {

	public NoSwing() {
		super("NoSwing", KEY_UNBOUND, ModuleCategory.MISC, "Makes you not swing your hand.",
				new SettingToggle("Client", true).withDesc("Makes you not swing your hand clientside."),
				new SettingToggle("Server", true).withDesc("Makes you not send hand swing packets."));
	}

	@BleachSubscribe
	public void onSwingHand(EventSwingHand event) {
		if (getSetting(0).asToggle().state) {
			event.setCancelled(true);
		}
	}

	@BleachSubscribe
	public void onSendPacket(EventSendPacket event) {
		if (event.getPacket() instanceof HandSwingC2SPacket && getSetting(1).asToggle().state) {
			event.setCancelled(true);
		}
	}
}
