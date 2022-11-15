/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import org.bleachhack.event.events.EventOpenScreen;
import org.bleachhack.event.events.EventPacket;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AutoSign extends Module {

	public String[] text = new String[0];

	public AutoSign() {
		super("AutoSign", KEY_UNBOUND, ModuleCategory.PLAYER, "Automatically writes on signs.",
				new SettingToggle("Random", false).withDesc("Writes random unicode in the sign.").withChildren(
						new SettingSlider("Length", 1, 1000, 500, 0).withDesc("How many characters to write per line.")));
	}

	@Override
	public void onDisable(boolean inWorld) {
		text = new String[0];

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void sendPacket(EventPacket.Send event) {
		if (event.getPacket() instanceof UpdateSignC2SPacket && text.length < 3) {
			text = ((UpdateSignC2SPacket) event.getPacket()).getText();
		}
	}

	@BleachSubscribe
	public void onOpenScreen(EventOpenScreen event) {
		if (text.length < 3)
			return;

		if (event.getScreen() instanceof SignEditScreen) {
			event.setCancelled(true);

			if (getSetting(0).asToggle().getState()) {
				text = new String[] {};
				while (text.length < 4) {
					IntStream chars = new Random().ints(0, 0x10FFFF);
					int amount = getSetting(0).asToggle().getChild(0).asSlider().getValueInt();
					text = chars.limit(amount * 5)
							.mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining())
							.split("(?<=\\G.{" + amount + "})");
				}
			}

			SignBlockEntity sign = ((SignEditScreen) event.getScreen()).sign;
			mc.player.networkHandler.sendPacket(new UpdateSignC2SPacket(sign.getPos(), text[0], text[1], text[2], text[3]));
		}
	}
}
