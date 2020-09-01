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

public class LogoutSpots extends Module {

    public LogoutSpots() {
        super("LogoutSpots", KEY_UNBOUND, Category.RENDER, "draws box where player logged out",
                new SettingColor("Player Color", 1f, 0.0f, 0.0f, false).withDesc("Logout spot color for players")
        );
    }


    @Subscribe
    public void onWorldEntityRender(EventWorldRenderEntity event) {

        if (event.entity instanceof PlayerEntity && event.entity != mc.player && this.isToggled()) {
            if (!BleachHack.friendMang.has(event.entity.getName().asString())) {
                float[] col = getSetting(0).asColor().getRGBFloat();
                event.vertex = getOutline(event.buffers, col[0], col[1], col[2]);
                RenderUtils.drawOutlineBox(event.entity.getBoundingBox(), col[0], col[1], col[2], 1f);
            }
        }

    }

    private VertexConsumerProvider getOutline(BufferBuilderStorage buffers, float r, float g, float b) {
        OutlineVertexConsumerProvider ovsp = buffers.getOutlineVertexConsumers();
        ovsp.setColor((int) (r * 255), (int) (g * 255), (int) (b * 255), 255);
        return ovsp;
    }
}
