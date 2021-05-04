/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import bleach.hack.setting.base.SettingToggle;
import bleach.hack.setting.other.SettingRotate;
import bleach.hack.util.InventoryUtils;
import bleach.hack.util.world.WorldUtils;
import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.util.FabricReflect;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/**
 * AutoMLG is a modified copy of https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/minegame159/meteorclient/systems/modules/movement/NoFall.java
 * Modified by <a href="https://github.com/lasnikprogram">Lasnik</a>
 */
public class Nofall extends Module {

    private boolean altBool, placedWater, centeredPlayer;
    private double x, z;

    public Nofall() {
        super("Nofall", KEY_UNBOUND, Category.PLAYER, "Prevents you from taking fall damage.",
                new SettingMode("Mode", "Simple", "Packet", "AutoMLG", "ec.me").withDesc("What mode to use"),
                new SettingToggle("Autocenter", false).withDesc("Autocenters you to the nearest block for AutoMLG"),
                new SettingRotate(true).withDesc("Rotates to floor for AutoMLG. Server option recommended"));

    }

    @Subscribe
    public void onTick(EventTick event) {
        if (mc.player.fallDistance > 2.5f && getSetting(0).asMode().mode == 0) {
            if (mc.player.isFallFlying())
                return;
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket(true));
        }

        if (mc.player.fallDistance > 2.5f && getSetting(0).asMode().mode == 1 &&
                mc.world.getBlockState(mc.player.getBlockPos().add(
                        0, -1.5 + (mc.player.getVelocity().y * 0.1), 0)).getBlock() != Blocks.AIR) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket(false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(
                    mc.player.getX(), mc.player.getY() - 420.69, mc.player.getZ(), true));
            mc.player.fallDistance = 0;
        }

        if (getSetting(0).asMode().mode == 2) {
            if (placedWater && mc.player.fallDistance == 0) {
                int slot = InventoryUtils.getSlot(true, i -> mc.player.inventory.getStack(i <= 9 || i == 40 ? i : 0).getItem() == Items.BUCKET);

                if (slot != -1 && mc.player.getBlockState().getFluidState().getFluid() == Fluids.WATER) {
                    useBucket(slot, false);
                }
                centeredPlayer = false;
                placedWater = false;
            } else if (mc.player.fallDistance > 2.5f && !isAboveWater()) {
                int slot = InventoryUtils.getSlot(true, i -> mc.player.inventory.getStack(i <= 9 || i == 40 ? i : 0).getItem() == Items.WATER_BUCKET);

                if (getSetting(1).asToggle().state) {
                    if (!centeredPlayer || x != mc.player.getX() || z != mc.player.getZ()) {
                        Vec3d centerPos = Vec3d.of(mc.player.getBlockPos()).add(0.5, 0, 0.5);
                        mc.player.updatePosition(centerPos.x, centerPos.y, centerPos.z);
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(centerPos.x, centerPos.y, centerPos.z, mc.player.isOnGround()));

                        x = mc.player.getX();
                        z = mc.player.getZ();
                        centeredPlayer = true;
                        mc.player.setVelocity(new Vec3d(0, mc.player.getVelocity().y, 0));
                    }
                }

                if (slot != -1) {
                    BlockHitResult result = mc.world.raycast(new RaycastContext(mc.player.getPos(), mc.player.getPos().subtract(0, 5, 0), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));

                    if (result != null && result.getType() == HitResult.Type.BLOCK) {
                        useBucket(slot, true);
                    }
                }
            }
        }
    }

    private boolean isAboveWater() {
        BlockPos.Mutable blockPos = mc.player.getBlockPos().mutableCopy();

        for (int i = 0; i < 64; i++) {
            BlockState state = mc.world.getBlockState(blockPos);

            if (state.getMaterial().blocksMovement()) break;

            Fluid fluid = state.getFluidState().getFluid();
            if (fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER) {
                return true;
            }

            blockPos.move(0, -1, 0);
        }
        return false;
    }

    private void useBucket(int slot, boolean setPlacedWater) {
        if (getSetting(2).asRotate().state) {
            WorldUtils.facePosAuto(mc.player.getX(), mc.player.getY() - 1, mc.player.getZ(), getSetting(2).asRotate());
        }
        int preSlot = mc.player.inventory.selectedSlot;
        mc.player.inventory.selectedSlot = slot;
        mc.interactionManager.interactItem(mc.player, mc.world, slot == 40 ? Hand.OFF_HAND : Hand.MAIN_HAND);
        mc.player.inventory.selectedSlot = preSlot;

        placedWater = setPlacedWater;
    }

    @Subscribe
    public void onSendPacket(EventSendPacket event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket && getSetting(0).asMode().mode == 3) {
            if (mc.player.fallDistance > 2.5f && !altBool) {
                mc.player.setOnGround(true);
                FabricReflect.writeField(event.getPacket(), true, "field_12891", "onGround");
                altBool = true;
            } else {
                altBool = false;
            }
        }
    }
}
