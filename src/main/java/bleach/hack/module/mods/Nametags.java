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

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventEntityRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.EntityUtils;
import bleach.hack.utils.WorldRenderUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.Objects;

public class Nametags extends Module {
    public int theirPing;
    public Nametags() {
        super("Nametags", KEY_UNBOUND, Category.RENDER, "Shows bigger/cooler nametags above entities.",
                new SettingMode("Armor", "H", "V", "None").withDesc("How to show items/armor"),
                new SettingMode("Health", "Number", "Bar", "Percent").withDesc("How to show health"),
                new SettingToggle("Players", true).withDesc("show player nametags").withChildren(
                        new SettingSlider("Size", 0.5, 5, 2, 1).withDesc("Size of the nametags")),
                new SettingToggle("Mobs", false).withDesc("show mobs/animal nametags").withChildren(
                        new SettingSlider("Size", 0.5, 5, 1, 1).withDesc("Size of the nametags")),
                new SettingToggle("Items", true).withDesc("Shows nametags for items").withChildren(
                        new SettingToggle("Custom Name", true).withDesc("Shows the items custom name if it has it"),
                        new SettingSlider("Size", 0.5, 5, 1, 1).withDesc("Size of the nametags")));
    }

    @Subscribe
    public void onLivingLabelRender(EventEntityRender.Label event) {
        if (((event.getEntity() instanceof Monster || EntityUtils.isAnimal(event.getEntity())) && getSetting(3).asToggle().state)
                || (event.getEntity() instanceof PlayerEntity && getSetting(2).asToggle().state)
                || (event.getEntity() instanceof ItemEntity && getSetting(4).asToggle().state))
            event.setCancelled(true);
    }

    @Subscribe
    public void onLivingRender(EventEntityRender.Render event) {
        if (event.getEntity() instanceof ItemEntity && getSetting(4).asToggle().state) {
            ItemEntity e = (ItemEntity) event.getEntity();

            double scale = Math.max(getSetting(4).asToggle().getChild(1).asSlider().getValue() * (mc.cameraEntity.distanceTo(e) / 20), 1);
            if (!e.getName().getString().equals(e.getStack().getName().getString()) && getSetting(4).asToggle().getChild(0).asToggle().state) {
                WorldRenderUtils.drawText("\u00a76\"" + e.getStack().getName().getString() + "\"",
                        e.prevX + (e.getX() - e.prevX) * mc.getTickDelta(),
                        (e.prevY + (e.getY() - e.prevY) * mc.getTickDelta()) + e.getHeight() + (0.75f * scale),
                        e.prevZ + (e.getZ() - e.prevZ) * mc.getTickDelta(), scale);
            }

            WorldRenderUtils.drawText("\u00A79" + e.getStack().getCount() + "x " + e.getName().getString(),
                    e.prevX + (e.getX() - e.prevX) * mc.getTickDelta(),
                    (e.prevY + (e.getY() - e.prevY) * mc.getTickDelta()) + e.getHeight() + (0.5f * scale),
                    e.prevZ + (e.getZ() - e.prevZ) * mc.getTickDelta(), scale);
        } else if (event.getEntity() instanceof LivingEntity) {
            LivingEntity e = (LivingEntity) event.getEntity();

            // Color before name
            String color = e instanceof Monster ? "\u00a75" : EntityUtils.isAnimal(e)
                    ? "\u00a7a" : e.isSneaking() ? "\u00a76" : e instanceof PlayerEntity ? "\u00a7c" : "\u00a7f";

            if (e == mc.player || e == mc.player.getVehicle() || color == "\u00a7f" ||
                    ((color == "\u00a7c" || color == "\u00a76") && !getSetting(2).asToggle().state) ||
                    ((color == "\u00a75" || color == "\u00a7a") && !getSetting(3).asToggle().state)) return;
            if (e.isInvisible()) color = "\u00a7e";

            double scale = (e instanceof PlayerEntity ?
                    Math.max(getSetting(2).asToggle().getChild(0).asSlider().getValue() * (mc.cameraEntity.distanceTo(e) / 20), 1) :
                    Math.max(getSetting(3).asToggle().getChild(0).asSlider().getValue() * (mc.cameraEntity.distanceTo(e) / 20), 1));


            /* Drawing Nametags */
            if (getSetting(1).asMode().mode == 0) {
                int ping = Objects.requireNonNull(mc.player.networkHandler.getPlayerListEntry(e.getName().getString())).getLatency();
                if (BleachHack.friendMang.has(e.getName().getString())) {
                    WorldRenderUtils.drawText("\u00A7b" + e.getName().getString() + " " + getPingColor(ping) + ping + "ms " + getHealthColor(e) + (int) (e.getHealth() + e.getAbsorptionAmount()),
                            e.prevX + (e.getX() - e.prevX) * mc.getTickDelta(),
                            (e.prevY + (e.getY() - e.prevY) * mc.getTickDelta()) + e.getHeight() + (0.5f * scale),
                            e.prevZ + (e.getZ() - e.prevZ) * mc.getTickDelta(), scale);
                } else {
                    WorldRenderUtils.drawText("\u00A7c" + e.getName().getString() + " " + getPingColor(ping) + ping + "ms " + getHealthColor(e) + (int) (e.getHealth() + e.getAbsorptionAmount()),
                            e.prevX + (e.getX() - e.prevX) * mc.getTickDelta(),
                            (e.prevY + (e.getY() - e.prevY) * mc.getTickDelta()) + e.getHeight() + (0.5f * scale),
                            e.prevZ + (e.getZ() - e.prevZ) * mc.getTickDelta(), scale);
                }
            } else if (getSetting(1).asMode().mode == 1) {
                /* Health bar */
                String health = "";
                /* - Add Green Normal Health */
                for (int i = 0; i < e.getHealth(); i++) health += "\u00a7a|";
                /* - Add Red Empty Health (Remove Based on absorption amount) */
                for (int i = 0; i < MathHelper.clamp(e.getAbsorptionAmount(), 0, e.getMaxHealth() - e.getHealth()); i++)
                    health += "\u00a7e|";
                /* Add Yellow Absorption Health */
                for (int i = 0; i < e.getMaxHealth() - (e.getHealth() + e.getAbsorptionAmount()); i++)
                    health += "\u00a7c|";
                /* Add "+??" to the end if the entity has extra hearts */
                if (e.getAbsorptionAmount() - (e.getMaxHealth() - e.getHealth()) > 0) {
                    health += " \u00a7e+" + (int) (e.getAbsorptionAmount() - (e.getMaxHealth() - e.getHealth()));
                }

                WorldRenderUtils.drawText(color + e.getName().getString(),
                        e.prevX + (e.getX() - e.prevX) * mc.getTickDelta(),
                        (e.prevY + (e.getY() - e.prevY) * mc.getTickDelta()) + e.getHeight() + (0.5f * scale),
                        e.prevZ + (e.getZ() - e.prevZ) * mc.getTickDelta(), scale);
                WorldRenderUtils.drawText(health,
                        e.prevX + (e.getX() - e.prevX) * mc.getTickDelta(),
                        (e.prevY + (e.getY() - e.prevY) * mc.getTickDelta()) + e.getHeight() + (0.75f * scale),
                        e.prevZ + (e.getZ() - e.prevZ) * mc.getTickDelta(), scale);
            } else if (getSetting(1).asMode().mode == 2) {
                WorldRenderUtils.drawText(color + e.getName().getString()
                                + getHealthColor(e) + " " + (int) ((e.getHealth() + e.getAbsorptionAmount()) / e.getMaxHealth() * 100) + "%",
                        e.prevX + (e.getX() - e.prevX) * mc.getTickDelta(),
                        (e.prevY + (e.getY() - e.prevY) * mc.getTickDelta()) + e.getHeight() + (0.5f * scale),
                        e.prevZ + (e.getZ() - e.prevZ) * mc.getTickDelta(), scale);
            }

            /* Drawing Items */
            //double c = 0;
            //double higher = getSetting(1).asMode().mode == 1 ? 0.25 : 0;
            //if (getSetting(0).asMode().mode == 0) {
            //    WorldRenderUtils.drawItem(e.prevX + (e.getX() - e.prevX) * mc.getTickDelta(),
            //            (e.prevY + (e.getY() - e.prevY) * mc.getTickDelta()) + e.getHeight() + ((0.75 + higher) * scale),
            //            e.prevZ + (e.getZ() - e.prevZ) * mc.getTickDelta(), -2.5, 0, scale, e.getEquippedStack(EquipmentSlot.MAINHAND));
            //    WorldRenderUtils.drawItem(e.prevX + (e.getX() - e.prevX) * mc.getTickDelta(),
            //            (e.prevY + (e.getY() - e.prevY) * mc.getTickDelta()) + e.getHeight() + ((0.75 + higher) * scale),
            //            e.prevZ + (e.getZ() - e.prevZ) * mc.getTickDelta(), 2.5, 0, scale, e.getEquippedStack(EquipmentSlot.OFFHAND));
            //    for (ItemStack i: e.getArmorItems()) {
            //        WorldRenderUtils.drawItem(e.prevX + (e.getX() - e.prevX) * mc.getTickDelta(),
            //                (e.prevY + (e.getY() - e.prevY) * mc.getTickDelta()) + e.getHeight() + ((0.75 + higher) * scale),
            //                e.prevZ + (e.getZ() - e.prevZ) * mc.getTickDelta(), c+1.5, 0, scale, i);
            //        c--;
            //    }
            //} else if (getSetting(0).asMode().mode == 1) {
            //    WorldRenderUtils.drawItem(e.prevX + (e.getX() - e.prevX) * mc.getTickDelta(),
            //            (e.prevY + (e.getY() - e.prevY) * mc.getTickDelta()) + e.getHeight() + ((0.75 + higher) * scale),
            //            e.prevZ + (e.getZ() - e.prevZ) * mc.getTickDelta(), -1.25, 0, scale, e.getEquippedStack(EquipmentSlot.MAINHAND));
            //    WorldRenderUtils.drawItem(e.prevX + (e.getX() - e.prevX) * mc.getTickDelta(),
            //            (e.prevY + (e.getY() - e.prevY) * mc.getTickDelta()) + e.getHeight() + ((0.75 + higher) * scale),
            //            e.prevZ + (e.getZ() - e.prevZ) * mc.getTickDelta(), 1.25, 0, scale, e.getEquippedStack(EquipmentSlot.OFFHAND));
            //    for (ItemStack i: e.getArmorItems()) {
            //        if (i.getCount() < 1) continue;
            //        WorldRenderUtils.drawItem(e.prevX + (e.getX() - e.prevX) * mc.getTickDelta(),
            //                (e.prevY + (e.getY() - e.prevY) * mc.getTickDelta()) + e.getHeight() + ((0.75 + higher) * scale),
            //                e.prevZ + (e.getZ() - e.prevZ) * mc.getTickDelta(), 0, c, scale, i);
            //        c++;
            //    }
            //}

            event.setCancelled(true);
        }
    }
    private String getPingColor(int ping) {
        if (ping < 100) {
            return "\u00a7a";
        } else if (ping < 150) {
            return "\u00a7c";
        } else {
            return "\u00a74";
        }
    }
    private String getHealthColor(LivingEntity entity) {
        if (entity.getHealth() + entity.getAbsorptionAmount() >= entity.getMaxHealth() * 0.7) {
            return "\u00a7a";
        } else if (entity.getHealth() + entity.getAbsorptionAmount() >= entity.getMaxHealth() * 0.1) {
            return "\u00a7c";
        } else {
            return "\u00a74";
        }
    }
}
