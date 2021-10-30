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

import bleach.hack.event.events.EventTick;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.other.SettingItemList;
import bleach.hack.module.Module;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.InventoryUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class AutoIStackDupe extends Module {
	
	private int tick = 0;

	public AutoIStackDupe() {
		super("AutoIStackDupe", KEY_UNBOUND, ModuleCategory.EXPLOITS, "Automatically does the IllegalStack dupe.",
				new SettingItemList("Items", "Items to dupe",
						Items.CHEST,
						Items.BLACK_SHULKER_BOX,
						Items.BLUE_SHULKER_BOX,
						Items.BROWN_SHULKER_BOX,
						Items.CYAN_SHULKER_BOX,
						Items.GRAY_SHULKER_BOX,
						Items.GREEN_SHULKER_BOX,
						Items.LIGHT_BLUE_SHULKER_BOX,
						Items.LIGHT_GRAY_SHULKER_BOX,
						Items.LIME_SHULKER_BOX,
						Items.MAGENTA_SHULKER_BOX,
						Items.ORANGE_SHULKER_BOX,
						Items.PINK_SHULKER_BOX,
						Items.PURPLE_SHULKER_BOX,
						Items.RED_SHULKER_BOX,
						Items.WHITE_SHULKER_BOX,
						Items.YELLOW_SHULKER_BOX,
						Items.SHULKER_BOX));
	}
	
	@Override
	public void onDisable(boolean inWorld) {
		tick = 0;
		
		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		Entity e = mc.world.getOtherEntities(null, mc.player.getBoundingBox().expand(3), en -> !mc.player.isConnectedThroughVehicle(en) && en instanceof AbstractDonkeyEntity && ((AbstractDonkeyEntity) en).isTame()).stream().findFirst().orElse(null);
		if (e == null)
			return;

		//BleachLogger.info(Integer.toString(tick));
		
		if (tick == 0) {
			if (((AbstractDonkeyEntity) e).hasChest()) {
				tick++;
				return;
			}

			Hand hand = InventoryUtils.selectSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.CHEST);
			if (hand != null) {
				mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(e, false, hand));
				mc.player.interact(e, hand);
				tick++;
			} else if (mc.player.currentScreenHandler == mc.player.playerScreenHandler) {
				BleachLogger.info("no chests left");
				setEnabled(false);
			}
		} else if (tick == 1) {
			tick++;
		} else if (tick == 2) {
			if (!((AbstractDonkeyEntity) e).hasChest()) {
				tick = 0;
				return;
			}

			if (mc.player.currentScreenHandler instanceof HorseScreenHandler) {
				tick++;
				return;
			}

			mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(e, true, Hand.MAIN_HAND));
			mc.player.interact(e, Hand.MAIN_HAND);
			tick++;
		} else if (tick == 3) {
			ScreenHandler sh = mc.player.currentScreenHandler;
			
			if (!((AbstractDonkeyEntity) e).hasChest() || sh.slots.size() <= 38) {
				tick = 0;
				return;
			}

			if (sh instanceof HorseScreenHandler) {
				int s = sh.slots.size() - 36;
				for (int ds = 2; ds <= s; ds++) {
					if (!sh.getSlot(ds).hasStack()) {
						int slot = InventoryUtils.getSlot(false, i -> canDupe(mc.player.getInventory().getStack(i)));
						if (slot != -1) {
							mc.interactionManager.clickSlot(sh.syncId, (slot < 9 ? slot + s + 27 : slot + s - 9), 0, SlotActionType.QUICK_MOVE, mc.player);
							return;
						} else if (ds == 2) {
							return;
						} else {
							tick++;
							return;
						}
					}
				}
				
				tick++;
			} else {
				tick = 1;
			}
		} else if (tick == 4) {
			//mc.player.closeHandledScreen();
			mc.player.closeScreen();
			tick++;
		} else if (tick == 5) {
			//mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interact(e, true, Hand.MAIN_HAND));
			mc.player.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interactAt(e, true, Hand.MAIN_HAND, mc.player.getPos()));
			tick++;
		} else {
			tick++;
			if (tick == 20)
				tick = 0;
		}
	}

	private boolean canDupe(ItemStack i) {
		return getSetting(0).asList(Item.class).contains(i.getItem());
	}
}
