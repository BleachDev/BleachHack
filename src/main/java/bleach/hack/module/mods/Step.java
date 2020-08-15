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

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.utils.WorldUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Step extends Module {

    private boolean flag;
    private double pos;

    public Step() {
        super("Step", KEY_UNBOUND, Category.MOVEMENT, "Allows you to Run up blocks like stairs.",
                new SettingMode("Mode", "Simple", "Spider", "Jump"));
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.player.stepHeight = 0.5F;
    }

    @Subscribe
    public void onTick(EventTick event) {
        if (!WorldUtils.NONSOLID_BLOCKS.contains(
                mc.world.getBlockState(mc.player.getBlockPos().add(0, mc.player.getHeight() + 1, 0)).getBlock()))
            return;

        if (getSetting(0).asMode().mode == 0) {
            mc.player.stepHeight = 1.065F;
        } else if (getSetting(0).asMode().mode == 1) {

            if (!mc.player.horizontalCollision && flag) {
                mc.player.setVelocity(mc.player.getVelocity().x, -0.1, mc.player.getVelocity().z);
            } else if (mc.player.horizontalCollision) {
                mc.player.setVelocity(mc.player.getVelocity().x, 1, mc.player.getVelocity().z);
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket(true));
                mc.player.jump();
                flag = true;
            }

            if (!mc.player.horizontalCollision) flag = false;

        } else if (getSetting(0).asMode().mode == 2) {

            if (mc.player.horizontalCollision && mc.player.isOnGround()) {
                pos = mc.player.getY();
                mc.player.jump();
                flag = true;
            }

            if (flag && pos + 1.065 < mc.player.getY()) {
                mc.player.setVelocity(mc.player.getVelocity().x, -0.1, mc.player.getVelocity().z);
                flag = false;
            }
        }
    }
}
