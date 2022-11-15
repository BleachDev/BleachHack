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
import org.bleachhack.setting.module.SettingToggle;

import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoTotem extends Module {

	private int delay;
	private boolean holdingTotem;

	public AutoTotem() {
		super("AutoTotem", KEY_UNBOUND, ModuleCategory.COMBAT, "Automatically equips totems.",
				new SettingToggle("Override", false).withDesc("Equips a totem even if theres another item in the offhand."),
				new SettingSlider("Delay", 0, 10, 0, 0).withDesc("Minimum delay between equipping totems (in ticks)."),
				new SettingSlider("PopDelay", 0, 10, 0, 0).withDesc("How long to wait after popping to equip a new totem (in ticks)."));
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (holdingTotem && mc.player.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
			delay = Math.max(getSetting(2).asSlider().getValueInt(), delay);
		}

		holdingTotem = mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;

		if (delay > 0) {
			delay--;
			return;
		}

		if (holdingTotem || (!mc.player.getOffHandStack().isEmpty() && !getSetting(0).asToggle().getState())) {
			return;
		}

		// Cancel at all non-survival-inventory containers
		if (mc.player.playerScreenHandler == mc.player.currentScreenHandler) {
			for (int i = 9; i < 45; i++) {
				if (mc.player.getInventory().getStack(i >= 36 ? i - 36 : i).getItem() == Items.TOTEM_OF_UNDYING) {
					boolean itemInOffhand = !mc.player.getOffHandStack().isEmpty();
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);

					if (itemInOffhand)
						mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.PICKUP, mc.player);

					delay = getSetting(1).asSlider().getValueInt();
					return;
				}
			}
		} else {
			// If the player is in another inventory, atleast check the hotbar for anything to swap
			for (int i = 0; i < 9; i++) {
				if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
					if (i != mc.player.getInventory().selectedSlot) {
						mc.player.getInventory().selectedSlot = i;
						mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(i));
					}

					mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
					delay = getSetting(1).asSlider().getValueInt();
					return;
				}
			}
		}
	}

}
