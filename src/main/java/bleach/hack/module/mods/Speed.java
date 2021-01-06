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
import com.google.common.eventbus.Subscribe;

public class Speed extends Module {

    private boolean jumping;

    public Speed() {
        super("Speed", KEY_UNBOUND, Category.MOVEMENT, "Allows you to go faster, what did you expect?",
                new SettingMode("Mode", "Bhop", "MiniHop", "OnGround", "Vanilla"),
                new SettingSlider("Move Speed", 0.1, 10, 2, 2));
    }

    @Subscribe
    public void onTick(EventTick event) {
        if (mc.options.keySneak.isPressed()) return;
        double speeds = getSetting(1).asSlider().getValue() / 30;
        double speedstrafe = getSetting(1).asSlider().getValue() / 3;

        /* OnGround */
        if (getSetting(0).asMode().mode == 2) {
            if (mc.options.keyJump.isPressed() || mc.player.fallDistance > 0.25) return;

            if (jumping && mc.player.getY() >= mc.player.prevY + 0.399994D) {
                mc.player.setVelocity(mc.player.getVelocity().x, -0.9, mc.player.getVelocity().z);
                mc.player.setPos(mc.player.getX(), mc.player.prevY, mc.player.getZ());
                jumping = false;
            }

            if (mc.player.forwardSpeed != 0.0F && !mc.player.horizontalCollision) {
                if (mc.player.verticalCollision) {
                    mc.player.setVelocity(mc.player.getVelocity().x * (0.85 + speeds), mc.player.getVelocity().y, mc.player.getVelocity().z * (0.85 + speeds));
                    jumping = true;
                    mc.player.jump();
                    // 1.0379
                }

                if (jumping && mc.player.getY() >= mc.player.prevY + 0.399994D) {
                    mc.player.setVelocity(mc.player.getVelocity().x, -100, mc.player.getVelocity().z);
                    jumping = false;
                }

            }

            /* MiniHop */
        } else if (getSetting(0).asMode().mode == 1) {
            if (mc.player.horizontalCollision || mc.options.keyJump.isPressed() || mc.player.forwardSpeed == 0) return;
            if (mc.player.isOnGround()) mc.player.jump();
            else if (mc.player.getVelocity().y > 0) {
                mc.player.setVelocity(mc.player.getVelocity().x * (0.9 + speeds), -1, mc.player.getVelocity().z * (0.9 + speeds));
                mc.player.input.movementSideways += 1.5F;
            }

            /* Bhop */
        } else if (getSetting(0).asMode().mode == 0) {
            if (mc.player.forwardSpeed > 0 && mc.player.isOnGround()) {
                mc.player.jump();
                mc.player.setVelocity(mc.player.getVelocity().x * (0.65 + speeds), 0.255556, mc.player.getVelocity().z * (0.65 + speeds));
                mc.player.sidewaysSpeed += 3.0F;
                mc.player.jump();
                mc.player.setSprinting(true);
            }
        }
        /* Vanilla */
        double forward = mc.player.forwardSpeed;
        double strafe = mc.player.sidewaysSpeed;
        float yaw = mc.player.yaw;

        if (getSetting(0).asMode().mode == 3 && !mc.player.isFallFlying()) {
            if ((forward == 0.0D) && (strafe == 0.0D)) {
                mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
            } else {
                if (forward != 0.0D) {
                    if (strafe > 0.0D) {
                        yaw += (forward > 0.0D ? -45 : 45);
                    } else if (strafe < 0.0D) yaw += (forward > 0.0D ? 45 : -45);
                    strafe = 0.0D;
                    if (forward > 0.0D) {
                        forward = 1.0D;
                    } else if (forward < 0.0D) forward = -1.0D;
                }
                mc.player.setVelocity((forward * speedstrafe * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speedstrafe * Math.sin(Math.toRadians(yaw + 90.0F))), mc.player.getVelocity().y,
                        forward * speedstrafe * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speedstrafe * Math.cos(Math.toRadians(yaw + 90.0F)));
            }
        }
    }
}
