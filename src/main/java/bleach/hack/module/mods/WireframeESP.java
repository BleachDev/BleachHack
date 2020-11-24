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

import bleach.hack.event.events.EventWorldRenderEntity;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.EntityUtils;
import bleach.hack.utils.OutlineRenderUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;

public class WireframeESP extends Module {

    public boolean drawOutlineLayer = false;

    public WireframeESP() {
        super("WireframeESP", KEY_UNBOUND, Category.RENDER, "Allows you to see entities though walls. (rlly scuffed dont use)",
                new SettingToggle("Players", true).withDesc("Show Players").withChildren(
                        new SettingSlider("Width", 0.1, 10, 2.5, 2).withDesc("How thick the lines should be"),
                        new SettingColor("Player Color", 1f, 0.3f, 0.3f, false).withDesc("Outline color for players"),
                        new SettingColor("Friend Color", 0f, 1f, 1f, false).withDesc("Outline color for friends")),

                new SettingToggle("Mobs", false).withDesc("Show Mobs").withChildren(
                        new SettingSlider("Width", 0.1, 10, 2.5, 2).withDesc("How thick the lines should be"),
                        new SettingColor("Color", 0.5f, 0.1f, 0.5f, false).withDesc("Outline color for mobs")),

                new SettingToggle("Animals", false).withDesc("Show Animals").withChildren(
                        new SettingSlider("Width", 0.1, 10, 2.5, 2).withDesc("How thick the lines should be"),
                        new SettingColor("Color", 0.3f, 1f, 0.3f, false).withDesc("Outline color for animals")),

                new SettingToggle("Items", true).withDesc("Show Items").withChildren(
                        new SettingSlider("Width", 0.1, 10, 2.5, 2).withDesc("How thick the lines should be"),
                        new SettingColor("Color", 1f, 0.8f, 0.2f, false).withDesc("Outline color for items")),

                new SettingToggle("Crystals", true).withDesc("Show End Crystals").withChildren(
                        new SettingSlider("Width", 0.1, 10, 2.5, 2).withDesc("How thick the lines should be"),
                        new SettingColor("Color", 1f, 0.2f, 1f, false).withDesc("Outline color for crystals")),

                new SettingToggle("Vehicles", false).withDesc("Show Vehicles").withChildren(
                        new SettingSlider("Width", 0.1, 10, 2.5, 2).withDesc("How thick the lines should be"),
                        new SettingColor("Color", 0.6f, 0.6f, 0.6f, false).withDesc("Outline color for vehicles (minecarts/boats)")),

                new SettingToggle("Donkeys", false).withDesc("Show Donkeys and Llamas for duping").withChildren(
                        new SettingSlider("Width", 0.1, 10, 2.5, 2).withDesc("How thick the lines should be"),
                        new SettingColor("Color", 0f, 0f, 1f, false).withDesc("Outline color for donkeys")));
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Subscribe
    public void onWorldEntityRender(EventWorldRenderEntity event) {
        if (event.entity instanceof PlayerEntity && event.entity != mc.player && getSetting(0).asToggle().state) {
            // Manually draw on players because of 2 colors
            float[] col = getSetting(0).asToggle().getChild(1).asColor().getRGBFloat();

            handleEsp(0, event, col[0], col[1], col[2]);

        } else if (event.entity instanceof Monster && getSetting(1).asToggle().state) {
            float[] col = getSetting(1).asToggle().getChild(1).asColor().getRGBFloat();
            handleEsp(1, event, col[0], col[1], col[2]);
        } // Before animals to prevent animals from overlapping donkeys
        else if (event.entity instanceof AbstractDonkeyEntity && getSetting(6).asToggle().state) {
            float[] col = getSetting(6).asToggle().getChild(1).asColor().getRGBFloat();
            handleEsp(6, event, col[0], col[1], col[2]);
        } else if (EntityUtils.isAnimal(event.entity) && getSetting(2).asToggle().state) {
            float[] col = getSetting(2).asToggle().getChild(1).asColor().getRGBFloat();
            handleEsp(2, event, col[0], col[1], col[2]);
        } else if (event.entity instanceof ItemEntity && getSetting(3).asToggle().state) {
            float[] col = getSetting(3).asToggle().getChild(1).asColor().getRGBFloat();
            handleEsp(3, event, col[0], col[1], col[2]);
        } else if (event.entity instanceof EndCrystalEntity && getSetting(4).asToggle().state) {
            float[] col = getSetting(4).asToggle().getChild(1).asColor().getRGBFloat();
            handleEsp(4, event, col[0], col[1], col[2]);
        } else if ((event.entity instanceof BoatEntity || event.entity instanceof AbstractMinecartEntity) && getSetting(5).asToggle().state) {
            float[] col = getSetting(5).asToggle().getChild(1).asColor().getRGBFloat();
            handleEsp(5, event, col[0], col[1], col[2]);
        }
    }

    private void handleEsp(int setting, EventWorldRenderEntity event, float r, float g, float b) {


        if (getSetting(setting).asToggle().state) {
            drawOutline(event.entity, event.matrix, r, g, b,(float) getSetting(setting).asToggle().getChild(0).asSlider().getValue());
        }
    }


    private void drawOutline(Entity entity, MatrixStack matrices, float r, float g, float b, float l) {
        matrices.push();
        OutlineRenderUtils.renderPass(entity, matrices, r, g, b, 0.2f);
        OutlineRenderUtils.renderOne(l);
        OutlineRenderUtils.renderPass(entity, matrices, r, g, b, 0.2f);
        OutlineRenderUtils.renderTwo();
        OutlineRenderUtils.renderPass(entity, matrices, r, g, b, 0.2f);
        OutlineRenderUtils.renderThree();
        OutlineRenderUtils.renderPass(entity, matrices, r, g, b, 0.2f);
        OutlineRenderUtils.renderFour();
        matrices.pop();
    }
}