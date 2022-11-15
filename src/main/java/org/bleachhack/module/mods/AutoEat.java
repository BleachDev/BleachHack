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
import org.bleachhack.util.InventoryUtils;

import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.FoodComponents;
import net.minecraft.util.Hand;

public class AutoEat extends Module {

	private boolean eating;

	public AutoEat() {
		super("AutoEat", KEY_UNBOUND, ModuleCategory.PLAYER, "Automatically eats food for you.",
				new SettingToggle("Hunger", true).withDesc("Eats when you're bewlow a certain amount of hunger.").withChildren(
						new SettingSlider("Hunger", 0, 19, 14, 0).withDesc("The maximum hunger to eat at.")),
				new SettingToggle("Health", false).withDesc("Eats when you're bewlow a certain amount of health.").withChildren(
						new SettingSlider("Health", 0, 19, 14, 0).withDesc("The maximum health to eat at.")),
				new SettingToggle("Gapples", true).withDesc("Eats golden apples.").withChildren(
						new SettingToggle("Prefer", false).withDesc("Prefers golden apples avobe regular food.")),
				new SettingToggle("Chorus", false).withDesc("Eats chorus fruit."),
				new SettingToggle("Poisonous", false).withDesc("Eats poisonous food."));
	}

	@Override
	public void onDisable(boolean inWorld) {
		mc.options.useKey.setPressed(false);

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (eating && mc.options.useKey.isPressed() && !mc.player.isUsingItem()) {
			eating = false;
			mc.options.useKey.setPressed(false);
		}

		if (getSetting(0).asToggle().getState() && mc.player.getHungerManager().getFoodLevel() <= getSetting(0).asToggle().getChild(0).asSlider().getValueInt()) {
			startEating();
		} else if (getSetting(1).asToggle().getState() && (int) mc.player.getHealth() + (int) mc.player.getAbsorptionAmount() <= getSetting(1).asToggle().getChild(0).asSlider().getValueInt()) {
			startEating();
		}
	}

	private void startEating() {
		boolean gapples = getSetting(2).asToggle().getState();
		boolean preferGapples = getSetting(2).asToggle().getChild(0).asToggle().getState();
		boolean chorus = getSetting(3).asToggle().getState();
		boolean poison = getSetting(4).asToggle().getState();

		int slot = -1;
		int hunger = -1;
		for (int s: InventoryUtils.getInventorySlots(true)) {
			FoodComponent food = mc.player.getInventory().getStack(s).getItem().getFoodComponent();

			if (food == null)
				continue;

			int h = preferGapples && (food == FoodComponents.GOLDEN_APPLE || food == FoodComponents.ENCHANTED_GOLDEN_APPLE)
					? Integer.MAX_VALUE : food.getHunger();

			if (h <= hunger
					|| (!gapples && (food == FoodComponents.GOLDEN_APPLE || food == FoodComponents.ENCHANTED_GOLDEN_APPLE))
					|| (!chorus && food == FoodComponents.CHORUS_FRUIT)
					|| (!poison && isPoisonous(food)))
				continue;

			slot = s;
			hunger = h;
		}

		if (hunger != -1) {
			if (slot == mc.player.getInventory().selectedSlot || slot == 40) {
				mc.options.useKey.setPressed(true);
				mc.interactionManager.interactItem(mc.player, slot == 40 ? Hand.OFF_HAND : Hand.MAIN_HAND);
				eating = true;
			} else {
				InventoryUtils.selectSlot(slot);
			}
		}
	}

	private boolean isPoisonous(FoodComponent food) {
		return food.getStatusEffects().stream().anyMatch(e -> e.getFirst().getEffectType().getCategory() == StatusEffectCategory.HARMFUL);
	}
}
