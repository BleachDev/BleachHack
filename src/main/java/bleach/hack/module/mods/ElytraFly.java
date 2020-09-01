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

import bleach.hack.event.events.EventClientMove;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends Module {

    public ElytraFly() {
        super("ElytraFly", KEY_UNBOUND, Category.MOVEMENT, "Improves the elytra",
                new SettingToggle("Auto Open", true).withDesc("Auto open elytra (only works with control) WIP"),
                new SettingMode("Mode", "Normal", "Control"),
                new SettingSlider("Overworld Speed", 0, 5, 1.28, 2),
                new SettingSlider("Nether Speed", 0, 5, 2.49, 2),
                new SettingSlider("End Speed", 0, 5, 1.28, 2),
                new SettingToggle("2b2t Downwards Velocity", false));
    }

    @Subscribe
    public void onClientMove(EventClientMove event) {
        /* Cancel the retarded auto elytra movement */
        if (getSetting(1).asMode().mode == 1 && mc.player.isFallFlying()) {
            if (!mc.options.keyJump.isPressed() && !mc.options.keySneak.isPressed()) {
                if (getSetting(5).asToggle().state) {
                    event.vec3d = new Vec3d(event.vec3d.x, -0.0001, event.vec3d.z);
                } else {
                    event.vec3d = new Vec3d(event.vec3d.x, 0, event.vec3d.z);
                }
            }

            if (!mc.options.keyBack.isPressed() && !mc.options.keyLeft.isPressed()
                    && !mc.options.keyRight.isPressed() && !mc.options.keyForward.isPressed()) {
                if (getSetting(5).asToggle().state) {
                    event.vec3d = new Vec3d(0, event.vec3d.y-0.0001, 0);
                } else {
                    event.vec3d = new Vec3d(0, event.vec3d.y, 0);
                }
            }
        }
    }

    @Subscribe
    public void onTick(EventTick event) {
        assert mc.world != null;
        Vec3d vec3d;
        if (mc.world.getRegistryKey().getValue().getPath().equalsIgnoreCase("the_end")) {
            vec3d = new Vec3d(0, 0, getSetting(4).asSlider().getValue()).rotateX(getSetting(1).asMode().mode == 1 ? 0 : -(float) Math.toRadians(mc.player.pitch)).rotateY(-(float) Math.toRadians(mc.player.yaw));
        } else if (mc.world.getRegistryKey().getValue().getPath().equalsIgnoreCase("the_nether")) {
            vec3d = new Vec3d(0, 0, getSetting(3).asSlider().getValue()).rotateX(getSetting(1).asMode().mode == 1 ? 0 : -(float) Math.toRadians(mc.player.pitch)).rotateY(-(float) Math.toRadians(mc.player.yaw));
        } else {
            vec3d = new Vec3d(0, 0, getSetting(2).asSlider().getValue()).rotateX(getSetting(1).asMode().mode == 1 ? 0 : -(float) Math.toRadians(mc.player.pitch)).rotateY(-(float) Math.toRadians(mc.player.yaw));
        }
        if (!mc.player.isFallFlying() && !mc.player.isOnGround() && getSetting(1).asMode().mode == 1 && mc.player.age % 10 == 0 && getSetting(0).asToggle().state) {
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
        }
        if (mc.player.isFallFlying()) {
            if (getSetting(1).asMode().mode == 0 && mc.options.keyForward.isPressed()) {
                mc.player.setVelocity(
                        mc.player.getVelocity().x + vec3d.x + (vec3d.x - mc.player.getVelocity().x),
                        mc.player.getVelocity().y + vec3d.y + (vec3d.y - mc.player.getVelocity().y),
                        mc.player.getVelocity().z + vec3d.z + (vec3d.z - mc.player.getVelocity().z));
            } else if (getSetting(1).asMode().mode == 1) {
                if (mc.options.keyBack.isPressed()) vec3d = vec3d.multiply(-1);
                if (mc.options.keyLeft.isPressed()) vec3d = vec3d.rotateY((float) Math.toRadians(90));
                if (mc.options.keyRight.isPressed()) vec3d = vec3d.rotateY(-(float) Math.toRadians(90));

                if (mc.options.keyJump.isPressed()) vec3d = vec3d.add(0, getSetting(2).asSlider().getValue(), 0);
                if (mc.options.keySneak.isPressed()) vec3d = vec3d.add(0, -getSetting(2).asSlider().getValue(), 0);

                if (!mc.options.keyBack.isPressed() && !mc.options.keyLeft.isPressed()
                        && !mc.options.keyRight.isPressed() && !mc.options.keyForward.isPressed()
                        && !mc.options.keyJump.isPressed() && !mc.options.keySneak.isPressed()) vec3d = Vec3d.ZERO;
                mc.player.setVelocity(vec3d.multiply(2));
            }
        }
    }
}
