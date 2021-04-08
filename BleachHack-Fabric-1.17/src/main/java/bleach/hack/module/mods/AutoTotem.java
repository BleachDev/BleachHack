/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingToggle;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoTotem extends Module {

	public AutoTotem() {
		super("AutoTotem", KEY_UNBOUND, Category.COMBAT, "Automatically equips totems.",
				new SettingToggle("Override", false).withDesc("Equips a totem even if theres another item in the offhand"));
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING
				|| (!mc.player.getOffHandStack().isEmpty() && !getSetting(0).asToggle().state))
			return;

		// Cancel at all non-survival-inventory containers
		if (mc.player.playerScreenHandler == mc.player.currentScreenHandler) {
			for (int i = 9; i < 45; i++) {
				if (mc.player.getInventory().getStack(i >= 36 ? i - 36 : i).getItem() == Items.TOTEM_OF_UNDYING) {
					boolean itemInOffhand = !mc.player.getOffHandStack().isEmpty();
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);

					if (itemInOffhand)
						mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.PICKUP, mc.player);

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
					return;
				}
			}
		}
	}

}
