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

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.utils.FabricReflect;
import bleach.hack.utils.WorldUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Flight extends Module {

    private boolean flyTick = false;

    public Flight() {
        super("Flight", KEY_UNBOUND, Category.MOVEMENT, "Allows you to fly",
                new SettingMode("Mode", "Normal", "Static", "Jetpack", "ec.me"),
                new SettingSlider("Speed", 0, 5, 1, 1),
                new SettingMode("AntiKick", "Off", "Fall", "Bob", "Packet"));
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (!mc.player.abilities.creativeMode) mc.player.abilities.allowFlying = false;
        mc.player.abilities.flying = false;
    }

    @Subscribe
    public void onTick(EventTick event) {
        float speed = (float) getSetting(1).asSlider().getValue();

        if (mc.player.age % 20 == 0 && getSetting(2).asMode().mode == 3 && !(getSetting(0).asMode().mode == 2)) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getY() - 0.06, mc.player.getZ(), false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), mc.player.getZ() + 0.06, mc.player.getZ(), true));
        }

        if (getSetting(0).asMode().mode == 0) {
            mc.player.abilities.setFlySpeed(speed / 10);
            mc.player.abilities.allowFlying = true;
            mc.player.abilities.flying = true;
        } else if (getSetting(0).asMode().mode == 1) {
            if (getSetting(2).asMode().mode == 0 || getSetting(2).asMode().mode == 3) mc.player.setVelocity(0, 0, 0);
            else if (getSetting(2).asMode().mode == 1 && WorldUtils.NONSOLID_BLOCKS.contains(mc.world.getBlockState(new BlockPos(mc.player.getPos().getX(), mc.player.getPos().getY() - 0.069, mc.player.getPos().getZ())).getBlock()))
                mc.player.setVelocity(0, mc.player.age % 20 == 0 ? -0.069 : 0, 0);
            else if (getSetting(2).asMode().mode == 2)
                mc.player.setVelocity(0, mc.player.age % 40 == 0 ? (WorldUtils.NONSOLID_BLOCKS.contains(mc.world.getBlockState(new BlockPos(mc.player.getPos().getX(), mc.player.getPos().getY() + 1.15, mc.player.getPos().getZ())).getBlock()) ? 0.15 : 0) : mc.player.age % 20 == 0 ? (WorldUtils.NONSOLID_BLOCKS.contains(mc.world.getBlockState(new BlockPos(mc.player.getPos().getX(), mc.player.getPos().getY() - 0.15, mc.player.getPos().getZ())).getBlock()) ? -0.15 : 0) : 0, 0);
            Vec3d forward = new Vec3d(0, 0, speed).rotateY(-(float) Math.toRadians(mc.player.yaw));
            Vec3d strafe = forward.rotateY((float) Math.toRadians(90));

            if (mc.options.keyJump.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(0, speed, 0));
            if (mc.options.keySneak.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(0, -speed, 0));
            if (mc.options.keyBack.isPressed())
                mc.player.setVelocity(mc.player.getVelocity().add(-forward.x, 0, -forward.z));
            if (mc.options.keyForward.isPressed())
                mc.player.setVelocity(mc.player.getVelocity().add(forward.x, 0, forward.z));
            if (mc.options.keyLeft.isPressed())
                mc.player.setVelocity(mc.player.getVelocity().add(strafe.x, 0, strafe.z));
            if (mc.options.keyRight.isPressed())
                mc.player.setVelocity(mc.player.getVelocity().add(-strafe.x, 0, -strafe.z));

        } else if (getSetting(0).asMode().mode == 2) {
            if (!mc.options.keyJump.isPressed()) return;
            mc.player.setVelocity(mc.player.getVelocity().x, speed / 3, mc.player.getVelocity().z);
        } else if (getSetting(0).asMode().mode == 3) {
            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.keyJump.getBoundKeyTranslationKey()).getCode())) {
                mc.player.jump();
            } else {
                if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), InputUtil.fromTranslationKey(mc.options.keyJump.getBoundKeyTranslationKey()).getCode())) {
                    mc.player.updatePosition(mc.player.getX(), mc.player.getY() - speed / 10f, mc.player.getZ());
                }
            }
        }
    }

    @Subscribe
    public void onSendPacket(EventSendPacket event) {
        if (getSetting(0).asMode().mode == 3 && event.getPacket() instanceof PlayerMoveC2SPacket) {
            if (!flyTick) {
                boolean onGround = true;//mc.player.fallDistance >= 0.1f;
                mc.player.setOnGround(onGround);
                FabricReflect.writeField(event.getPacket(), onGround, "field_12891", "onGround");

                flyTick = true;
            } else {
                flyTick = false;
            }
        }
    }
}
