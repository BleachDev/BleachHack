/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.BleachHack;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.world.DamageUtils;
import org.bleachhack.util.world.EntityUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;

import net.minecraft.text.Text;

public class AutoLog extends Module {

	private boolean smartDisabled = false;

	public AutoLog() {
		super("AutoLog", KEY_UNBOUND, ModuleCategory.COMBAT, "Automatically disconnects from servers.",
				new SettingToggle("Health", true).withDesc("Disconnects when you're under a certain health.").withChildren(
						new SettingSlider("Health", 1, 20, 5, 0).withDesc("The health to disconnects at."),
						new SettingToggle("IgnoreTotems", false).withDesc("Makes you disconnect even if you're carrying totems."),
						new SettingToggle("Vehicle", false).withDesc("Also disconnects when your vehicle is below the specified health.")),
				new SettingToggle("OneHit", true).withDesc("Disconnects when a nearby player can kill you in one hit.").withChildren(
						new SettingToggle("IgnoreFriends", true).withDesc("Makes you not disconnect if the player is on your friend list.")),
				new SettingToggle("Crystal", false).withDesc("Disconnects when you're near an end crystal.").withChildren(
						new SettingSlider("Distance", 0.1, 14, 6, 1).withDesc("The maximum distance away from a crystal to disconnect.")),
				new SettingToggle("PlayerNearby", false).withDesc("Disconnects when a player is in render distance/nearby").withChildren(
						new SettingToggle("Range", false).withDesc("Disconnects when a player is inside a range instead of in render distance.").withChildren(
								new SettingSlider("Range", 1, 200, 50, 0).withDesc("The range to disconnect at.")),
						new SettingToggle("IgnoreFriends", true).withDesc("Makes you not disconnect if the player is on your friend list.")),
				new SettingToggle("SmartToggle", false).withDesc("Re-enables AutoLog after you rejoin and aren't meeting the log requirements.").withChildren(
						new SettingToggle("Warn", false).withDesc("Shows in the chat when AutoLog re-enables.")));
	}

	@Override
	public void onDisable(boolean inWorld) {
		smartDisabled = false;

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		Text logText = getLogText();
		
		if (smartDisabled && logText == null) {
			smartDisabled = false;
			
			if (getSetting(4).asToggle().getChild(0).asToggle().getState()) {
				BleachLogger.info("Re-enabled AutoLog!");
			}
		} else if (!smartDisabled && logText != null) {
			log(logText);
		}
	}

	private Text getLogText() {
		boolean hasTotem = mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING 
				|| mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;

		int playerHealth = (int) (mc.player.getHealth() + mc.player.getAbsorptionAmount());

		if (getSetting(0).asToggle().getState()) {
			int health = getSetting(0).asToggle().getChild(0).asSlider().getValueInt();

			if ((getSetting(0).asToggle().getChild(1).asToggle().getState() || !hasTotem) && playerHealth <= health) {
				return Text.literal("[AutoLog] Your health (" + playerHealth + " HP) was lower than " + health + " HP.");
			}

			if (getSetting(0).asToggle().getChild(2).asToggle().getState() &&  mc.player.getVehicle() instanceof LivingEntity) {
				LivingEntity vehicle = (LivingEntity) mc.player.getVehicle();
				int vehicleHealth = (int) (vehicle.getHealth() + vehicle.getAbsorptionAmount());

				if (vehicleHealth < health) {
					return Text.literal("[AutoLog] Your vehicle health (" + vehicleHealth + " HP) was lower than " + health + " HP.");
				}
			}
		}

		if (getSetting(1).asToggle().getState() && !hasTotem) {
			for (PlayerEntity player: mc.world.getPlayers()) {
				if ((!getSetting(1).asToggle().getChild(0).asToggle().getState() && BleachHack.friendMang.has(player))
						|| player == mc.player) {
					continue;
				}

				int attackDamage = (int) DamageUtils.getAttackDamage(player, mc.player);

				if (player.distanceTo(mc.player) <= 6 && attackDamage >= playerHealth) {
					return Text.literal("[AutoLog] " + player.getDisplayName().getString() + " could kill you (dealing " + attackDamage + " damage).");
				}
			}
		}

		if (getSetting(2).asToggle().getState()) {
			for (Entity e: mc.world.getEntities()) {
				if (e instanceof EndCrystalEntity && mc.player.distanceTo(e) <= getSetting(2).asToggle().getChild(0).asSlider().getValue()) {
					return Text.literal("[AutoLog] End crystal appeared within range.");
				}
			}
		}

		if (getSetting(3).asToggle().getState()) {
			double range = getSetting(3).asToggle().getChild(0).asToggle().getState()
					? getSetting(3).asToggle().getChild(0).asToggle().getChild(0).asSlider().getValue()
							: Double.MAX_VALUE;

			for (PlayerEntity player: mc.world.getPlayers()) {
				if (!EntityUtils.isOtherServerPlayer(player)
						|| (!getSetting(3).asToggle().getChild(1).asToggle().getState() && BleachHack.friendMang.has(player))) {
					continue;
				}

				if (player.distanceTo(mc.player) <= range) {
					return Text.literal("[AutoLog] " + player.getDisplayName().getString() + " appeared " + (int) player.distanceTo(mc.player) + " blocks away.");
				}
			}
		}

		return null;
	}

	private void log(Text reason) {
		mc.player.networkHandler.getConnection().disconnect(reason);

		if (getSetting(4).asToggle().getState()) {
			smartDisabled = true;
		} else {
			setEnabled(false);
		}
	}

}
