/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventPacket;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.InventoryUtils;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoEXP extends Module {

	private int delay;
	private int xpNeeded;
	private int slot = -1;

	public AutoEXP() {
		super("AutoEXP", KEY_UNBOUND, ModuleCategory.PLAYER, "Automatically uses XP bottles to repair items with mending.",
				new SettingToggle("Armor", true).withDesc("Uses XP when your armor durability is low."),
				new SettingToggle("MainHand", true).withDesc("Uses XP when your mainhand item durability is low."),
				new SettingToggle("OffHand", true).withDesc("Uses XP when your offhand item durability is low."),
				new SettingSlider("Durability", 0, 20, 5, 0).withDesc("How low the item dirability has to be before repairing."),
				new SettingSlider("Repair", 0, 1, 1, 2).withDesc("How much durability to repair."),
				new SettingSlider("XP/tick", 1, 10, 1, 0).withDesc("How many xp bottles to throw each batch."),
				new SettingSlider("Delay", 0, 10, 0, 0).withDesc("How long to wait before throwing each batch (in ticks)."));
	}

	@Override
	public void onDisable(boolean inWorld) {
		delay = 0;
		xpNeeded = 0;
		slot = -1;

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (mc.player.currentScreenHandler != mc.player.playerScreenHandler)
			return;

		int xpSlot = InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.EXPERIENCE_BOTTLE);
		if (xpSlot == -1)
			return;

		int damage = getSetting(3).asSlider().getValueInt();
		double target = getSetting(4).asSlider().getValue();

		if (slot != -1) {
			if (xpNeeded == 0) {
				ItemStack item = slot < 45 ? mc.player.currentScreenHandler.getSlot(slot).getStack() : mc.player.getOffHandStack();
				if (item.isDamaged() && item.getMaxDamage() - item.getDamage() <= damage)
					return;

				for (int i = 1; i <= 4; i++) {
					ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
					if (!stack.isEmpty()) {
						for (int j = 5; j <= 8; j++) {
							if (mc.player.currentScreenHandler.getSlot(j).canInsert(stack)) {
								mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
								mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, j, 0, SlotActionType.PICKUP, mc.player);
								return;
							}
						}
					}
				}

				if (slot >= 46) {
					if (slot - 46 != mc.player.getInventory().selectedSlot) {
						mc.player.getInventory().selectedSlot = slot - 46;
						mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot - 46));
					}

					mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
				}

				delay = 0;
				slot = -1;
				return;
			}

			for (int i = 5; i <= 8; i++) {
				if (i != slot && EnchantmentHelper.getLevel(Enchantments.MENDING, mc.player.currentScreenHandler.getSlot(i).getStack()) != 0) {
					for (int j = 1; j <= 4; j++) {
						ItemStack craftingStack = mc.player.currentScreenHandler.getSlot(j).getStack();
						if (!craftingStack.isDamageable()) {
							mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
							mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, j, 0, SlotActionType.PICKUP, mc.player);
							if (!craftingStack.isEmpty())
								mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, j, 1, SlotActionType.THROW, mc.player);

							return;
						}
					}
				}
			}

			if (slot > 8 && slot < 45) {
				if (slot - 36 != mc.player.getInventory().selectedSlot) {
					mc.player.getInventory().selectedSlot = slot - 36;
					mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot - 36));
				}

				mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
				slot += 10; // hack
				return;
			}

			delay++;
			if (delay >= getSetting(6).asSlider().getValueInt()) {
				delay = 0;
				int toThrow = Math.min(getSetting(5).asSlider().getValueInt(), xpNeeded);

				if (toThrow != 0) {
					mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(mc.player.getYaw(), 90, mc.player.isOnGround()));
					for (int t = 0; t < toThrow; t++) {
						if (InventoryUtils.selectSlot(false, i -> mc.player.getInventory().getStack(i).getItem() == Items.EXPERIENCE_BOTTLE) == Hand.MAIN_HAND) {
							// Trying to use without bruh
							mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0));
							ItemStack itemStack2 = mc.player.getMainHandStack().use(mc.world, mc.player, Hand.MAIN_HAND).getValue();
							if (itemStack2 != mc.player.getMainHandStack()) {
								mc.player.setStackInHand(Hand.MAIN_HAND, itemStack2);
							}

							xpNeeded--;
						}
					}
				}
			}

			return;
		}

		IntSet slots = new IntArraySet();
		if (getSetting(0).asToggle().getState()) {
			slots.add(5);
			slots.add(6);
			slots.add(7);
			slots.add(8);
		}

		if (getSetting(1).asToggle().getState())
			slots.add(36 + mc.player.getInventory().selectedSlot);

		if (getSetting(2).asToggle().getState())
			slots.add(45);

		if (getSetting(0).asToggle().getState()) {
			for (int s: slots) {
				ItemStack item = mc.player.currentScreenHandler.getSlot(s).getStack();

				if (item.isDamageable() && item.getMaxDamage() - item.getDamage() <= damage
						&& item.getMaxDamage() - item.getDamage() < item.getMaxDamage() * target) {
					slot = s;
					xpNeeded = (int) Math.ceil((item.getMaxDamage() * target - (item.getMaxDamage() - item.getDamage())) / 14d);
					return;
				}
			}
		}
	}

	@BleachSubscribe
	public void onSendPacket(EventPacket.Send event) {
		if (slot != -1 && event.getPacket() instanceof CloseHandledScreenC2SPacket) {
			event.setCancelled(true);
		}
	}
}
