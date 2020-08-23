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
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.utils.WorldUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class ArrowJuke extends Module {

    public ArrowJuke() {
        super("ArrowJuke", KEY_UNBOUND, Category.COMBAT, "Tries to dodge arrows coming at you",
                new SettingMode("Move", "Client", "Packet"),
                new SettingSlider("Speed", 0.01, 2, 1, 2));
    }

    @Subscribe
    public void onTick(EventTick event) {
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof ArrowEntity) || e.age > 50) continue;

            Box pBox = mc.player.getBoundingBox().expand(0.555);
            List<Box> boxes = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                Vec3d nextPos = e.getPos().add(e.getVelocity().multiply(i / 5));
                boxes.add(new Box(
                        nextPos.subtract(e.getBoundingBox().getXLength() / 2, 0, e.getBoundingBox().getZLength() / 2),
                        nextPos.add(e.getBoundingBox().getXLength() / 2, e.getBoundingBox().getYLength(), e.getBoundingBox().getZLength() / 2)));
            }

            int mode = getSetting(0).asMode().mode;
            double speed = getSetting(1).asSlider().getValue();

            for (int i = 0; i < 75; i++) {
                Vec3d nextPos = e.getPos().add(e.getVelocity().multiply(i / 5));
                Box nextBox = new Box(
                        nextPos.subtract(e.getBoundingBox().getXLength() / 2, 0, e.getBoundingBox().getZLength() / 2),
                        nextPos.add(e.getBoundingBox().getXLength() / 2, e.getBoundingBox().getYLength(), e.getBoundingBox().getZLength() / 2));

                if (pBox.intersects(nextBox)) {
                    for (Vec3d vel : new Vec3d[]{new Vec3d(1, 0, 0), new Vec3d(-1, 0, 0), new Vec3d(0, 0, 1)}) {
                        boolean contains = false;
                        for (Box b : boxes)
                            if (b.intersects(WorldUtils.moveBox(pBox, vel.x, vel.y, vel.z))) contains = true;
                        if (!contains) {
                            if (mode == 0) {
                                Vec3d vel2 = vel.multiply(speed);
                                mc.player.setVelocity(vel2.x, vel2.y, vel2.z);
                            } else if (mode == 1) {
                                Vec3d vel2 = mc.player.getPos().add(vel.multiply(speed));
                                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(vel2.x, vel2.y, vel2.z, false));
                                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(vel2.x, vel2.y - 0.01, vel2.z, true));
                            }
                            return;
                        }
                    }

                    if (mode == 0) {
                        mc.player.setVelocity(0, 0, -speed);
                    } else if (mode == 1) {
                        Vec3d vel2 = mc.player.getPos().add(new Vec3d(0, 0, -speed));
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(vel2.x, vel2.y, vel2.z, false));
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(vel2.x, vel2.y - 0.01, vel2.z, true));
                    }
                }
            }
        }
    }

}
