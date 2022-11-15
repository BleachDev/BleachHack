/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util;

import java.util.Comparator;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class InventoryUtils {
	
	private static final MinecraftClient mc = MinecraftClient.getInstance();

	/** Returns the slot with the <b>lowest</b> comparator value **/
	public static int getSlot(boolean offhand, boolean reverse, Comparator<Integer> comparator) {
		return IntStream.of(getInventorySlots(offhand))
				.boxed()
				.min(reverse ? comparator.reversed() : comparator).get();
	}

	/** Selects the slot with the <b>lowest</b> comparator value and returns the hand it selected **/
	public static Hand selectSlot(boolean offhand, boolean reverse, Comparator<Integer> comparator) {
		return selectSlot(getSlot(offhand, reverse, comparator));
	}
	
	/** Returns the first slot that matches the Predicate **/
	public static int getSlot(boolean offhand, IntPredicate filter) {
		return IntStream.of(getInventorySlots(offhand))
				.filter(filter)
				.findFirst().orElse(-1);
	}
	
	/** Selects the first slot that matches the Predicate and returns the hand it selected **/
	public static Hand selectSlot(boolean offhand, IntPredicate filter) {
		return selectSlot(getSlot(offhand, filter));
	}
	
	public static Hand selectSlot(int slot) {
		if (slot >= 0 && slot <= 36) {
			if (slot < 9) {
				if (slot != mc.player.getInventory().selectedSlot) {
					mc.player.getInventory().selectedSlot = slot;
					mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
				}

				return Hand.MAIN_HAND;
			} else if (mc.player.playerScreenHandler == mc.player.currentScreenHandler) {
				for (int i = 0; i <= 8; i++) {
					if (mc.player.getInventory().getStack(i).isEmpty()) {
						mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.QUICK_MOVE, mc.player);

						if (i != mc.player.getInventory().selectedSlot) {
							mc.player.getInventory().selectedSlot = i;
							mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(i));
						}

						return Hand.MAIN_HAND;
					}
				}

				mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
				mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 36 + mc.player.getInventory().selectedSlot, 0, SlotActionType.PICKUP, mc.player);
				mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
				return Hand.MAIN_HAND;
			}
		} else if (slot == 40) {
			return Hand.OFF_HAND;
		}

		return null;
	}
	
	public static int[] getInventorySlots(boolean offhand) {
		int[] i = new int[offhand ? 38 : 37];
		
		// Add hand slots first
		i[0] = mc.player.getInventory().selectedSlot;
		i[1] = 40;

		for (int j = 0; j < 36; j++) {
			if (j != mc.player.getInventory().selectedSlot) {
				i[offhand ? j + 2 : j + 1] = j;
			}
		}
		
		return i;
	}
}
