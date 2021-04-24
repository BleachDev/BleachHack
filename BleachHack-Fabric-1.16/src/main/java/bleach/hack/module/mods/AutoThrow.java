/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.InventoryUtils;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.util.Hand;

public class AutoThrow extends Module {

	private Map<StatusEffect, Integer> effectCache = new HashMap<>();
	private int delay = 0;

	public AutoThrow() {
		super("AutoThrow", KEY_UNBOUND, Category.PLAYER, "Automatically throws XP/Potions",
				new SettingToggle("XP", true).withDesc("Automatically throws XP"),
				new SettingToggle("Potions", true).withDesc("Automatically throws splash potions").withChildren(
						new SettingToggle("Smart", true).withDesc("Only throws if you don't have the effect already")),
				new SettingToggle("SwitchBack", true).withDesc("Switches back to your prevous item after throwing"),
				new SettingSlider("Delay", 0, 6, 4, 0).withDesc("How long to wait before throwing the next item (in ticks)"));
	}

	@Override
	public void onDisable() {
		effectCache.clear();
		super.onDisable();
	}

	@Subscribe
	public void onTick(EventTick event) {
		for (Entry<StatusEffect, Integer> e: new HashMap<>(effectCache).entrySet()) {
			if (e.getValue() <= 0) {
				effectCache.remove(e.getKey());
			} else {
				effectCache.replace(e.getKey(), e.getValue() - 1);
			}
		}

		if (delay > 0) {
			delay--;
			return;
		}

		if (getSetting(0).asToggle().state) {
			int slot = InventoryUtils.getSlot(true, i -> mc.player.inventory.getStack(i).getItem() == Items.EXPERIENCE_BOTTLE);

			if (slot != 1) {
				use(slot, getSetting(2).asToggle().state);
				return;
			}
		}

		if (getSetting(1).asToggle().state) {
			int slot = InventoryUtils.getSlot(true, i -> {
				if (mc.player.inventory.getStack(i).getItem() == Items.SPLASH_POTION) {
					Potion potion = PotionUtil.getPotion(mc.player.inventory.getStack(i));

					if (getSetting(1).asToggle().getChild(0).asToggle().state) {
						return !(potion.getEffects().isEmpty()
								|| potion.getEffects().stream().anyMatch(s -> s.getEffectType().getType() == StatusEffectType.HARMFUL)
								|| potion.getEffects().stream().allMatch(
										s -> s.getEffectType().isInstant()
										|| mc.player.hasStatusEffect(s.getEffectType())
										|| effectCache.containsKey(s.getEffectType())));
					} else {
						return !potion.getEffects().isEmpty()
								&& potion.getEffects().stream().allMatch(s -> s.getEffectType().getType() != StatusEffectType.HARMFUL);
					}
				}

				return false;
			});

			if (slot != 1) {
				PotionUtil.getPotion(mc.player.inventory.getStack(slot)).getEffects().forEach(s -> effectCache.put(s.getEffectType(), 100));
				use(slot, getSetting(2).asToggle().state);
				return;
			}
		}
	}

	private void use(int slot, boolean switchback) {
		if (slot != -1) {
			int prevSlot = slot < 9 && slot != mc.player.inventory.selectedSlot ? mc.player.inventory.selectedSlot : -1;

			InventoryUtils.selectSlot(slot);

			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(mc.player.yaw, 90, mc.player.isOnGround()));
			mc.interactionManager.interactItem(mc.player, mc.world, slot == 40 ? Hand.OFF_HAND : Hand.MAIN_HAND);

			if (prevSlot != -1) {
				mc.player.inventory.selectedSlot = prevSlot;
				mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prevSlot));
			}

			delay = getSetting(3).asSlider().getValueInt();
		}
	}
}
