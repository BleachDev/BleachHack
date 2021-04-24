/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import com.google.common.eventbus.Subscribe;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.world.DamageUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class AutoLog extends Module {

	private boolean smartDisabled = false;

	public AutoLog() {
		super("AutoLog", KEY_UNBOUND, Category.COMBAT, "Automatically disconnect from a server",
				new SettingToggle("Health", true).withDesc("Disconnects when you're under a certain health").withChildren(
						new SettingSlider("Health", 1, 20, 5, 0).withDesc("The health to log at"),
						new SettingToggle("IgnoreTotems", false).withDesc("Makes you disconnect even if you have totems"),
						new SettingToggle("Vehicle", false).withDesc("Also disconnects when your vehicle is below the specified health")),
				new SettingToggle("OneHit", true).withDesc("Disconnects when a nearby player can kill you in one hit").withChildren(
						new SettingToggle("IgnoreFriends", true).withDesc("Makes you not disconnect if its a friend")),
				new SettingToggle("Crystal", false).withDesc("Disconnects when you're near a crystal").withChildren(
						new SettingSlider("Distance", 0.1, 14, 6, 1).withDesc("The maximun distance to a crystal to log")),
				new SettingToggle("PlayerNearby", false).withDesc("Disconnects when a player is in render distance/nearby").withChildren(
						new SettingToggle("Range", false).withDesc("Disconnects when a player is inside the range instead of in render distance").withChildren(
								new SettingSlider("Range", 1, 200, 50, 0).withDesc("Range to diconnect at")),
						new SettingToggle("IgnoreFriends", true).withDesc("Makes you not disconnect if its a friend")),
				new SettingToggle("SmartToggle", false).withDesc("Re-enables AutoLog when you rejoin and are not meeting the log requirements").withChildren(
						new SettingToggle("Warn", false).withDesc("Shows in the chat when AutoLog re-enables")));
	}

	@Override
	public void onDisable() {
		smartDisabled = false;

		super.onDisable();
	}

	@Subscribe
	public void onTick(EventTick event) {
		Text logText = getLogText();
		
		if (smartDisabled && logText == null) {
			smartDisabled = false;
			
			if (getSetting(4).asToggle().getChild(0).asToggle().state) {
				BleachLogger.infoMessage("Re-enabled AutoLog!");
			}
		} else if (!smartDisabled && logText != null) {
			log(logText);
		}
	}

	private Text getLogText() {
		boolean hasTotem = mc.player.getMainHandStack().getItem() == Items.TOTEM_OF_UNDYING 
				|| mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;

		int playerHealth = (int) (mc.player.getHealth() + mc.player.getAbsorptionAmount());

		if (getSetting(0).asToggle().state) {
			int health = getSetting(0).asToggle().getChild(0).asSlider().getValueInt();

			if ((getSetting(0).asToggle().getChild(1).asToggle().state || !hasTotem) && playerHealth <= health) {
				return new LiteralText("[AutoLog] Your health (" + playerHealth + " HP) was lower than " + health + " HP.");
			}

			if (getSetting(0).asToggle().getChild(2).asToggle().state &&  mc.player.getVehicle() instanceof LivingEntity) {
				LivingEntity vehicle = (LivingEntity) mc.player.getVehicle();
				int vehicleHealth = (int) (vehicle.getHealth() + vehicle.getAbsorptionAmount());

				if (vehicleHealth < health) {
					return new LiteralText("[AutoLog] Your vehicle health (" + vehicleHealth + " HP) was lower than " + health + " HP.");
				}
			}
		}

		if (getSetting(1).asToggle().state && !hasTotem) {
			for (PlayerEntity player: mc.world.getPlayers()) {
				if ((!getSetting(1).asToggle().getChild(0).asToggle().state && BleachHack.friendMang.has(player.getDisplayName().getString()))
						|| player == mc.player) {
					continue;
				}

				int attackDamage = (int) DamageUtils.getAttackDamage(player, mc.player);

				if (player.distanceTo(mc.player) <= 6 && attackDamage >= playerHealth) {
					return new LiteralText("[AutoLog] " + player.getDisplayName().getString() + " could kill you (dealing " + attackDamage + " damage).");
				}
			}
		}

		if (getSetting(2).asToggle().state) {
			for (Entity e: mc.world.getEntities()) {
				if (e instanceof EndCrystalEntity && mc.player.distanceTo(e) <= getSetting(2).asToggle().getChild(0).asSlider().getValue()) {
					return new LiteralText("[AutoLog] End crystal appeared within range.");
				}
			}
		}

		if (getSetting(3).asToggle().state) {
			double range = getSetting(3).asToggle().getChild(0).asToggle().state
					? getSetting(3).asToggle().getChild(0).asToggle().getChild(0).asSlider().getValue()
							: Double.MAX_VALUE;

			for (PlayerEntity player: mc.world.getPlayers()) {
				if ((!getSetting(3).asToggle().getChild(1).asToggle().state && BleachHack.friendMang.has(player.getDisplayName().getString()))
						|| player == mc.player) {
					continue;
				}

				if (player.distanceTo(mc.player) <= range) {
					return new LiteralText("[AutoLog] " + player.getDisplayName().getString() + " appeared " + (int) player.distanceTo(mc.player) + " blocks away.");
				}
			}
		}

		return null;
	}

	private void log(Text reason) {
		mc.player.networkHandler.getConnection().disconnect(reason);

		if (getSetting(4).asToggle().state) {
			smartDisabled = true;
		} else {
			setEnabled(false);
		}
	}

}
