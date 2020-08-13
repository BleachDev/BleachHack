/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
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

import bleach.hack.event.events.EventEntityRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.EntityUtils;
import bleach.hack.utils.WorldRenderUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class Nametags extends Module {

	public Nametags() {
		super("Nametags", KEY_UNBOUND, Category.RENDER, "Shows bigger/cooler nametags above entities.",
				new SettingMode("Armor", "H", "V", "None").withDesc("How to show items/armor"),
				new SettingMode("Health", "Number", "Bar", "Percent").withDesc("How to show health"),
				new SettingToggle("Players", true).withDesc("show player nametags")
						.withChildren(new SettingSlider("Size", 0.5, 5, 2, 1).withDesc("Size of the nametags")),
				new SettingToggle("Mobs", false).withDesc("show mobs/animal nametags")
						.withChildren(new SettingSlider("Size", 0.5, 5, 1, 1).withDesc("Size of the nametags")),
				new SettingToggle("Items", true).withDesc("Shows nametags for items").withChildren(
						new SettingToggle("Custom Name", true).withDesc("Shows the items custom name if it has it"),
						new SettingSlider("Size", 0.5, 5, 1, 1).withDesc("Size of the nametags")));
	}

	@Subscribe
	public void onEntityRender(EventEntityRender.Render event) {
		if (((event.getEntity() instanceof Monster || EntityUtils.isAnimal(event.getEntity())) && getSetting(3).asToggle().state)
				|| (event.getEntity() instanceof PlayerEntity && getSetting(2).asToggle().state)
				|| (event.getEntity() instanceof ItemEntity && getSetting(4).asToggle().state))
			event.getEntity().setCustomNameVisible(true);
	}

	@Subscribe
	public void onEntityLabelRender(EventEntityRender.Label event) {
		if (((event.getEntity() instanceof Monster || EntityUtils.isAnimal(event.getEntity())) && getSetting(3).asToggle().state)
				|| (event.getEntity() instanceof PlayerEntity && getSetting(2).asToggle().state)
				|| (event.getEntity() instanceof ItemEntity && getSetting(4).asToggle().state))
			event.setCancelled(true);

		if (event.getEntity() instanceof ItemEntity && getSetting(4).asToggle().state) {
			ItemEntity e = (ItemEntity) event.getEntity();

			double scale = Math.max(getSetting(4).asToggle().getChild(1).asSlider().getValue() * (mc.cameraEntity.distanceTo(e) / 20), 1);
			if (!e.getName().getString().equals(e.getStack().getName().getString()) && getSetting(4).asToggle().getChild(0).asToggle().state) {
				WorldRenderUtils.drawText("§6\"" + e.getStack().getName().getString() + "\"", e.prevX + (e.x - e.prevX) * mc.getTickDelta(),
						(e.prevY + (e.y - e.prevY) * mc.getTickDelta()) + e.getHeight() + (0.75f * scale), e.prevZ + (e.z - e.prevZ) * mc.getTickDelta(), scale);
			}

			WorldRenderUtils.drawText("§6" + e.getName().getString() + " §e[x" + e.getStack().getCount() + "]", e.prevX + (e.x - e.prevX) * mc.getTickDelta(),
					(e.prevY + (e.y - e.prevY) * mc.getTickDelta()) + e.getHeight() + (0.5f * scale), e.prevZ + (e.z - e.prevZ) * mc.getTickDelta(), scale);
		} else if (event.getEntity() instanceof LivingEntity) {
			LivingEntity e = (LivingEntity) event.getEntity();

			/* Color before name */
			String color = e instanceof Monster ? "§5" : EntityUtils.isAnimal(e) ? "§a" : e.isSneaking() ? "§6" : e instanceof PlayerEntity ? "§c" : "§f";

			if (e == mc.player || e == mc.player.getVehicle() || color == "§f" || ((color == "§c" || color == "§6") && !getSetting(2).asToggle().state)
					|| ((color == "§5" || color == "§a") && !getSetting(3).asToggle().state))
				return;
			if (e.isInvisible())
				color = "§e";

			double scale = (e instanceof PlayerEntity ? Math.max(getSetting(2).asToggle().getChild(0).asSlider().getValue() * (mc.cameraEntity.distanceTo(e) / 20), 1)
					: Math.max(getSetting(3).asToggle().getChild(0).asSlider().getValue() * (mc.cameraEntity.distanceTo(e) / 20), 1));

			/* Drawing Nametags */
			if (getSetting(1).asMode().mode == 0) {
				WorldRenderUtils.drawText(
						color + e.getName().getString() + " §a[" + getHealthColor(e) + (int) (e.getHealth() + e.getAbsorptionAmount()) + "§a/"
								+ (int) e.getHealthMaximum() + "]",
						e.prevX + (e.x - e.prevX) * mc.getTickDelta(), (e.prevY + (e.y - e.prevY) * mc.getTickDelta()) + e.getHeight() + (0.5f * scale),
						e.prevZ + (e.z - e.prevZ) * mc.getTickDelta(), scale);
			} else if (getSetting(1).asMode().mode == 1) {
				/* Health bar */
				String health = "";
				/* - Add Green Normal Health */
				for (int i = 0; i < e.getHealth(); i++)
					health += "§a|";
				/* - Add Red Empty Health (Remove Based on absorption amount) */
				for (int i = 0; i < MathHelper.clamp(e.getAbsorptionAmount(), 0, e.getHealthMaximum() - e.getHealth()); i++)
					health += "§e|";
				/* Add Yellow Absorption Health */
				for (int i = 0; i < e.getHealthMaximum() - (e.getHealth() + e.getAbsorptionAmount()); i++)
					health += "§c|";
				/* Add "+??" to the end if the entity has extra hearts */
				if (e.getAbsorptionAmount() - (e.getHealthMaximum() - e.getHealth()) > 0) {
					health += " §e+" + (int) (e.getAbsorptionAmount() - (e.getHealthMaximum() - e.getHealth()));
				}

				WorldRenderUtils.drawText(color + e.getName().getString(), e.prevX + (e.x - e.prevX) * mc.getTickDelta(),
						(e.prevY + (e.y - e.prevY) * mc.getTickDelta()) + e.getHeight() + (0.5f * scale), e.prevZ + (e.z - e.prevZ) * mc.getTickDelta(), scale);
				WorldRenderUtils.drawText(health, e.prevX + (e.x - e.prevX) * mc.getTickDelta(),
						(e.prevY + (e.y - e.prevY) * mc.getTickDelta()) + e.getHeight() + (0.75f * scale), e.prevZ + (e.z - e.prevZ) * mc.getTickDelta(), scale);
			} else if (getSetting(1).asMode().mode == 2) {
				WorldRenderUtils.drawText(
						color + e.getName().getString() + getHealthColor(e) + " [" + (int) ((e.getHealth() + e.getAbsorptionAmount()) / e.getHealthMaximum() * 100)
								+ "%]",
						e.prevX + (e.x - e.prevX) * mc.getTickDelta(), (e.prevY + (e.y - e.prevY) * mc.getTickDelta()) + e.getHeight() + (0.5f * scale),
						e.prevZ + (e.z - e.prevZ) * mc.getTickDelta(), scale);
			}

			// Drawing Items
			double c = 0;
			double higher = getSetting(1).asMode().mode == 1 ? 0.25 : 0;

			if (getSetting(0).asMode().mode == 0) {
				WorldRenderUtils.drawItem(e.x, e.y + e.getHeight() + ((0.75 + higher) * scale), e.z, -2.5, 0, scale, e.getEquippedStack(EquipmentSlot.MAINHAND));
				WorldRenderUtils.drawItem(e.x, e.y + e.getHeight() + ((0.75 + higher) * scale), e.z, 2.5, 0, scale, e.getEquippedStack(EquipmentSlot.OFFHAND));

				for (ItemStack i : e.getArmorItems()) {
					WorldRenderUtils.drawItem(e.x, e.y + e.getHeight() + ((0.75 + higher) * scale), e.z, c + 1.5, 0, scale, i);
					c--;
				}
			} else if (getSetting(0).asMode().mode == 1) {
				WorldRenderUtils.drawItem(e.x, e.y + e.getHeight() + ((0.75 + higher) * scale), e.z, -1.25, 0, scale, e.getEquippedStack(EquipmentSlot.MAINHAND));
				WorldRenderUtils.drawItem(e.x, e.y + e.getHeight() + ((0.75 + higher) * scale), e.z, 1.25, 0, scale, e.getEquippedStack(EquipmentSlot.OFFHAND));

				for (ItemStack i : e.getArmorItems()) {
					if (i.getCount() < 1)
						continue;
					WorldRenderUtils.drawItem(e.x, e.y + e.getHeight() + ((0.75 + higher) * scale), e.z, 0, c, scale, i);
					c++;
				}
			}
		}
	}

	private String getHealthColor(LivingEntity entity) {
		if (entity.getHealth() + entity.getAbsorptionAmount() > entity.getHealthMaximum()) {
			return "§e";
		} else if (entity.getHealth() + entity.getAbsorptionAmount() >= entity.getHealthMaximum() * 0.7) {
			return "§a";
		} else if (entity.getHealth() + entity.getAbsorptionAmount() >= entity.getHealthMaximum() * 0.4) {
			return "§6";
		} else if (entity.getHealth() + entity.getAbsorptionAmount() >= entity.getHealthMaximum() * 0.1) {
			return "§c";
		} else {
			return "§4";
		}
	}
}
