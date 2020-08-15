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

import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.RenderUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.*;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

public class ChestESP extends Module {

    public ChestESP() {
        super("ChestESP", KEY_UNBOUND, Category.RENDER, "Draws a box around storage containers.",
                new SettingToggle("Chests", true),
                new SettingToggle("EndChests", true),
                new SettingToggle("Furnaces", true),
                new SettingToggle("Dispensers", true),
                new SettingToggle("Hoppers", true),
                new SettingToggle("Shulkers", true),
                new SettingToggle("BrewStands", true),
                new SettingToggle("ChestCarts", true),
                new SettingToggle("FurnaceCarts", true),
                new SettingToggle("HopperCarts", true),
                new SettingToggle("ItemFrames", true),
                new SettingToggle("ArmorStands", true));
    }

    @Subscribe
    public void onRender(EventWorldRender event) {
        List<BlockPos> linkedChests = new ArrayList<>();

        for (BlockEntity e : mc.world.blockEntities) {
            if (linkedChests.contains(e.getPos())) {
                continue;
            }

            if ((e instanceof ChestBlockEntity || e instanceof BarrelBlockEntity) && getSetting(0).asToggle().state) {
                BlockPos p = drawChest(e.getPos());
                if (p != null) linkedChests.add(p);
            } else if (e instanceof EnderChestBlockEntity && getSetting(1).asToggle().state) {
                RenderUtils.drawFilledBox(new Box(
                        e.getPos().getX() + 0.06, e.getPos().getY(), e.getPos().getZ() + 0.06,
                        e.getPos().getX() + 0.94, e.getPos().getY() + 0.875, e.getPos().getZ() + 0.94), 1F, 0.05F, 1F, 0.7F);
            } else if (e instanceof AbstractFurnaceBlockEntity && getSetting(2).asToggle().state) {
                RenderUtils.drawFilledBox(e.getPos(), 0.5F, 0.5F, 0.5F, 0.7F);
            } else if (e instanceof DispenserBlockEntity && getSetting(3).asToggle().state) {
                RenderUtils.drawFilledBox(e.getPos(), 0.55F, 0.55F, 0.7F, 0.7F);
            } else if (e instanceof HopperBlockEntity && getSetting(4).asToggle().state) {
                RenderUtils.drawFilledBox(e.getPos(), 0.45F, 0.45F, 0.6F, 0.7F);
            } else if (e instanceof ShulkerBoxBlockEntity && getSetting(5).asToggle().state) {
                RenderUtils.drawFilledBox(e.getPos(), 0.5F, 0.2F, 1F, 0.7F);
            } else if (e instanceof BrewingStandBlockEntity && getSetting(6).asToggle().state) {
                RenderUtils.drawFilledBox(e.getPos(), 0.5F, 0.4F, 0.2F, 0.7F);
            }
        }

        for (Entity e : mc.world.getEntities()) {
            if (e instanceof ChestMinecartEntity && getSetting(7).asToggle().state) {
                RenderUtils.drawFilledBox(e.getBoundingBox(), 1F, 0.65F, 0.3F, 0.7F);
            } else if (e instanceof FurnaceMinecartEntity && getSetting(8).asToggle().state) {
                RenderUtils.drawFilledBox(e.getBoundingBox(), 0.5F, 0.5F, 0.5F, 0.7F);
            } else if (e instanceof HopperMinecartEntity && getSetting(9).asToggle().state) {
                RenderUtils.drawFilledBox(e.getBoundingBox(), 0.45F, 0.45F, 0.6F, 0.7F);
            } else if (e instanceof ItemFrameEntity && getSetting(10).asToggle().state) {
                if (((ItemFrameEntity) e).getHeldItemStack().getItem() == Items.AIR) {
                    RenderUtils.drawFilledBox(e.getBoundingBox(), 0.45F, 0.1F, 0.1F, 0.7F);
                } else if (((ItemFrameEntity) e).getHeldItemStack().getItem() == Items.FILLED_MAP) {
                    int axis = e.getBoundingBox().maxX - e.getBoundingBox().minX < e.getBoundingBox().maxY - e.getBoundingBox().minY
                            ? 0 : e.getBoundingBox().maxY - e.getBoundingBox().minY < e.getBoundingBox().maxZ - e.getBoundingBox().minZ
                            ? 1 : 2;

                    RenderUtils.drawFilledBox(e.getBoundingBox().expand(axis == 0 ? 0 : 0.12, axis == 1 ? 0 : 0.12, axis == 2 ? 0 : 0.12), 0.1F, 0.1F, 0.5F, 0.7F);
                } else {
                    RenderUtils.drawFilledBox(e.getBoundingBox(), 0.1F, 0.45F, 0.1F, 0.7F);
                }
            }

            if (e instanceof ArmorStandEntity && getSetting(11).asToggle().state) {
                RenderUtils.drawFilledBox(e.getBoundingBox(), 0.5F, 0.4F, 0.1F, 0.7F);
            }
        }
    }

    /**
     * returns the other chest if its linked, othwise null
     **/
    private BlockPos drawChest(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);

        if (!(state.getBlock() instanceof ChestBlock)) {
            RenderUtils.drawFilledBox(pos, 1F, 0.6F, 0.3F, 0.7F);
            return null;
        }

        if (state.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE) {
            RenderUtils.drawFilledBox(new Box(
                    pos.getX() + 0.06, pos.getY(), pos.getZ() + 0.06,
                    pos.getX() + 0.94, pos.getY() + 0.875, pos.getZ() + 0.94), 1F, 0.6F, 0.3F, 0.7F);
            return null;
        }

        boolean north = false, east = false, south = false, west = false;

        Direction dir = ChestBlock.getFacing(state);
        if (dir == Direction.NORTH) {
            north = true;
        } else if (dir == Direction.EAST) {
            east = true;
        } else if (dir == Direction.SOUTH) {
            south = true;
        } else if (dir == Direction.WEST) {
            west = true;
        }

        RenderUtils.drawFilledBox(new Box(
                        west ? pos.getX() - 0.94 : pos.getX() + 0.06,
                        pos.getY(),
                        north ? pos.getZ() - 0.94 : pos.getZ() + 0.06,
                        east ? pos.getX() + 1.94 : pos.getX() + 0.94,
                        pos.getY() + 0.875,
                        south ? pos.getZ() + 1.94 : pos.getZ() + 0.94),
                1F, 0.6F, 0.3F, 0.7F);

        return north ? pos.north() : east ? pos.east() : south ? pos.south() : west ? pos.west() : null;
    }

}
