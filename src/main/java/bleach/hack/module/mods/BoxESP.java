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
import bleach.hack.event.events.EventWorldRenderEntity;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.EntityUtils;
import bleach.hack.utils.RenderUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoxESP extends Module {
    public List<String> strength_alert = new ArrayList<>();
    public BoxESP() {
        super("BoxESP", KEY_UNBOUND, Category.RENDER, "Allows you to see entities though walls.",
                new SettingToggle("Players", true).withDesc("Show Players").withChildren(
                        new SettingColor("Player Color", 1f, 0.3f, 0.3f, false).withDesc("Tracer color for players"),
                        new SettingColor("Friend Color", 0f, 1f, 1f, false).withDesc("Outline color for friends")),
                new SettingToggle("Mobs", false).withDesc("Show Mobs").withChildren(
                        new SettingColor("Color", 0.5f, 0.1f, 0.5f, false).withDesc("Outline color for mobs")),
                new SettingToggle("Animals", false).withDesc("Show Animals").withChildren(
                        new SettingColor("Color", 0.3f, 1f, 0.3f, false).withDesc("Outline color for animals")),
                new SettingToggle("Items", true).withDesc("Show Items").withChildren(
                        new SettingColor("Color", 85, 85, 255, false).withDesc("Outline color for items")),
                new SettingToggle("Crystals", true).withDesc("Show End Crystals").withChildren(
                        new SettingColor("Color", 1f, 0.2f, 1f, false).withDesc("Outline color for crystals")),
                new SettingToggle("Vehicles", false).withDesc("Show Vehicles").withChildren(
                        new SettingColor("Color", 0.6f, 0.6f, 0.6f, false).withDesc("Outline color for vehicles (minecarts/boats)")),
                new SettingToggle("Donkeys", false).withDesc("Show Donkeys and Llamas for duping").withChildren(
                        new SettingColor("Color", 0f, 0f, 1f, false).withDesc("Outline color for donkeys")),
                new SettingToggle("Strength", false).withDesc("Show red boxes around people with strength").withChildren(
                        new SettingToggle("Chat Alert", true).withDesc("Alerts you in chat when players in render drink strength")
                ));
    }


    @Subscribe
    public void onWorldEntityRender(EventWorldRenderEntity event) {

        if (event.entity instanceof PlayerEntity && event.entity != mc.player && getSetting(0).asToggle().state) {
            if (BleachHack.friendMang.has(event.entity.getName().asString())) {
                float[] col = getSetting(0).asToggle().getChild(1).asColor().getRGBFloat();
                event.vertex = getOutline(event.buffers, col[0], col[1], col[2]);
                RenderUtils.drawOutlineBox(event.entity.getBoundingBox(), col[0], col[1], col[2], 1f);
            } else {
                float[] col = getSetting(0).asToggle().getChild(0).asColor().getRGBFloat();
                event.vertex = getOutline(event.buffers, col[0], col[1], col[2]);
                RenderUtils.drawOutlineBox(event.entity.getBoundingBox(), col[0], col[1], col[2], 1f);
            }
        } else if (event.entity instanceof Monster && getSetting(1).asToggle().state) {
            float[] col = getSetting(1).asToggle().getChild(0).asColor().getRGBFloat();
            event.vertex = getOutline(event.buffers, col[0], col[1], col[2]);
            RenderUtils.drawOutlineBox(event.entity.getBoundingBox(), col[0], col[1], col[2], 1f);
        } // Before animals to prevent animals from overlapping donkeys
        else if (event.entity instanceof AbstractDonkeyEntity && getSetting(6).asToggle().state) {
            float[] col = getSetting(6).asToggle().getChild(0).asColor().getRGBFloat();
            event.vertex = getOutline(event.buffers, col[0], col[1], col[2]);
            RenderUtils.drawOutlineBox(event.entity.getBoundingBox(), col[0], col[1], col[2], 1f);
        } else if (EntityUtils.isAnimal(event.entity) && getSetting(2).asToggle().state) {
            float[] col = getSetting(2).asToggle().getChild(0).asColor().getRGBFloat();
            event.vertex = getOutline(event.buffers, col[0], col[1], col[2]);
            RenderUtils.drawOutlineBox(event.entity.getBoundingBox(), col[0], col[1], col[2], 1f);
        } else if (event.entity instanceof ItemEntity && getSetting(3).asToggle().state) {
            float[] col = getSetting(3).asToggle().getChild(0).asColor().getRGBFloat();
            event.vertex = getOutline(event.buffers, col[0], col[1], col[2]);
            RenderUtils.drawOutlineBox(event.entity.getBoundingBox(), col[0], col[1], col[2], 1f);
        } else if (event.entity instanceof EndCrystalEntity && getSetting(4).asToggle().state) {
            float[] col = getSetting(4).asToggle().getChild(0).asColor().getRGBFloat();
            event.vertex = getOutline(event.buffers, col[0], col[1], col[2]);
            RenderUtils.drawOutlineBox(event.entity.getBoundingBox(), col[0], col[1], col[2], 1f);
        } else if ((event.entity instanceof BoatEntity || event.entity instanceof AbstractMinecartEntity) && getSetting(5).asToggle().state) {
            float[] col = getSetting(5).asToggle().getChild(0).asColor().getRGBFloat();
            event.vertex = getOutline(event.buffers, col[0], col[1], col[2]);
            RenderUtils.drawOutlineBox(event.entity.getBoundingBox(), col[0], col[1], col[2], 1f);
        } else if ((event.entity instanceof PlayerEntity) && ((PlayerEntity) event.entity).hasStatusEffect(StatusEffects.STRENGTH) && this.getSettings().get(7).asToggle().state) {
            RenderUtils.drawFilledBox(event.entity.getBoundingBox(), 1.0F, 0.0F, 0.0F, 1F);
        }
        if (!strength_alert.toString().contains(event.entity.getUuidAsString()) && (event.entity instanceof PlayerEntity) && ((PlayerEntity) event.entity).hasStatusEffect(StatusEffects.STRENGTH) && this.getSettings().get(7).asToggle().state && this.getSettings().get(7).asToggle().getChild(0).asToggle().state) {
            BleachLogger.infoMessage(event.entity.getEntityName() + " has drank strength!");
            strength_alert.add(event.entity.getUuidAsString());
        } else if (strength_alert.toString().contains(event.entity.getUuidAsString()) && (event.entity instanceof PlayerEntity) && !((PlayerEntity) event.entity).hasStatusEffect(StatusEffects.STRENGTH)) {
            strength_alert.removeAll(Collections.singleton(event.entity.getUuidAsString()));
            BleachLogger.infoMessage(event.entity.getEntityName() + " no longer has strength!");
        }

    }

    private VertexConsumerProvider getOutline(BufferBuilderStorage buffers, float r, float g, float b) {
        OutlineVertexConsumerProvider ovsp = buffers.getOutlineVertexConsumers();
        ovsp.setColor((int) (r * 255), (int) (g * 255), (int) (b * 255), 255);
        return ovsp;
    }
    @Override
    public void onDisable() {
        super.onDisable();
        strength_alert.clear();
    }
}
