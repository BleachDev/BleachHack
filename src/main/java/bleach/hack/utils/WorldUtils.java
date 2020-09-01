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
package bleach.hack.utils;

import java.util.Arrays;
import java.util.List;

import bleach.hack.setting.other.SettingRotate;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class WorldUtils {

    protected static MinecraftClient mc = MinecraftClient.getInstance();

    public static final List<Block> NONSOLID_BLOCKS = Arrays.asList(
            Blocks.AIR, Blocks.LAVA, Blocks.WATER, Blocks.GRASS,
            Blocks.VINE, Blocks.SEAGRASS, Blocks.TALL_SEAGRASS,
            Blocks.SNOW, Blocks.TALL_GRASS, Blocks.FIRE, Blocks.VOID_AIR);

    public static final List<Block> RIGHTCLICKABLE_BLOCKS = Arrays.asList(
            Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST,
            Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX,
            Blocks.BLACK_SHULKER_BOX, Blocks.ANVIL,
            Blocks.OAK_BUTTON, Blocks.ACACIA_BUTTON, Blocks.BIRCH_BUTTON, Blocks.DARK_OAK_BUTTON,
            Blocks.JUNGLE_BUTTON, Blocks.SPRUCE_BUTTON, Blocks.STONE_BUTTON, Blocks.COMPARATOR,
            Blocks.REPEATER, Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.BIRCH_FENCE_GATE,
            Blocks.JUNGLE_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE, Blocks.ACACIA_FENCE_GATE,
            Blocks.BREWING_STAND, Blocks.DISPENSER, Blocks.DROPPER,
            Blocks.LEVER, Blocks.NOTE_BLOCK, Blocks.JUKEBOX,
            Blocks.BEACON, Blocks.BLACK_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.CYAN_BED, Blocks.GRAY_BED,
            Blocks.GREEN_BED, Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_GRAY_BED, Blocks.LIME_BED, Blocks.MAGENTA_BED,
            Blocks.ORANGE_BED, Blocks.PINK_BED, Blocks.PURPLE_BED, Blocks.RED_BED, Blocks.WHITE_BED,
            Blocks.YELLOW_BED, Blocks.FURNACE, Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR,
            Blocks.BIRCH_DOOR, Blocks.JUNGLE_DOOR, Blocks.ACACIA_DOOR,
            Blocks.DARK_OAK_DOOR, Blocks.CAKE, Blocks.ENCHANTING_TABLE,
            Blocks.DRAGON_EGG, Blocks.HOPPER, Blocks.REPEATING_COMMAND_BLOCK,
            Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.CRAFTING_TABLE,
            Blocks.ACACIA_TRAPDOOR, Blocks.BIRCH_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.JUNGLE_TRAPDOOR,
            Blocks.OAK_TRAPDOOR, Blocks.SPRUCE_TRAPDOOR, Blocks.CAKE, Blocks.ACACIA_SIGN, Blocks.ACACIA_WALL_SIGN,
            Blocks.BIRCH_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN,
            Blocks.JUNGLE_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.OAK_SIGN, Blocks.OAK_WALL_SIGN,
            Blocks.SPRUCE_SIGN, Blocks.SPRUCE_WALL_SIGN);

    public static boolean isFluid(BlockPos pos) {
        List<Material> fluids = Arrays.asList(Material.WATER, Material.LAVA, Material.UNDERWATER_PLANT);

        return fluids.contains(MinecraftClient.getInstance().world.getBlockState(pos).getMaterial());
    }

    public static boolean doesBoxTouchBlock(Box box, Block block) {
        for (int x = (int) Math.floor(box.minX); x < Math.ceil(box.maxX); x++) {
            for (int y = (int) Math.floor(box.minY); y < Math.ceil(box.maxY); y++) {
                for (int z = (int) Math.floor(box.minZ); z < Math.ceil(box.maxZ); z++) {
                    if (MinecraftClient.getInstance().world.getBlockState(new BlockPos(x, y, z)).getBlock() == block) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isBoxEmpty(Box box) {
        for (int x = (int) Math.floor(box.minX); x < Math.ceil(box.maxX); x++) {
            for (int y = (int) Math.floor(box.minY); y < Math.ceil(box.maxY); y++) {
                for (int z = (int) Math.floor(box.minZ); z < Math.ceil(box.maxZ); z++) {
                    if (!NONSOLID_BLOCKS.contains(MinecraftClient.getInstance().world.getBlockState(new BlockPos(x, y, z)).getBlock())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static Box moveBox(Box box, double x, double y, double z) {
        return new Box(new Vec3d(box.minX, box.minY, box.minZ).add(x, y, z), new Vec3d(box.maxX, box.maxY, box.maxZ).add(x, y, z));
    }

    public static boolean placeBlock(BlockPos pos, int slot, boolean rotate, boolean rotateBack) {
        if (pos.getY() < 0 || pos.getY() > 255 || !isBlockEmpty(pos))
            return false;

        if (slot != mc.player.inventory.selectedSlot && slot >= 0 && slot <= 8)
            mc.player.inventory.selectedSlot = slot;

        for (Direction d : Direction.values()) {
            if ((d == Direction.DOWN && pos.getY() == 0) || (d == Direction.UP && pos.getY() == 255))
                continue;

            Block neighborBlock = mc.world.getBlockState(pos.offset(d)).getBlock();

            Vec3d vec = new Vec3d(pos.getX() + 0.5 + d.getOffsetX() * 0.5,
                    pos.getY() + 0.5 + d.getOffsetY() * 0.5,
                    pos.getZ() + 0.5 + d.getOffsetZ() * 0.5);

            if (NONSOLID_BLOCKS.contains(neighborBlock)
                    || mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0).distanceTo(vec) > 4.55)
                continue;

            float[] rot = new float[] { mc.player.yaw, mc.player.pitch };

            if (rotate)
                facePosPacket(vec.x, vec.y, vec.z);
            if (RIGHTCLICKABLE_BLOCKS.contains(neighborBlock))
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.PRESS_SHIFT_KEY));

            System.out.println(mc.world.getBlockState(pos.offset(d)) + " | " + pos.offset(d));
            mc.interactionManager.interactBlock(
                    mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(Vec3d.of(pos), d.getOpposite(), pos.offset(d), false));

            if (RIGHTCLICKABLE_BLOCKS.contains(neighborBlock))
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.RELEASE_SHIFT_KEY));
            if (rotateBack)
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(rot[0], rot[1], mc.player.isOnGround()));
            return true;
        }
        return false;
    }

    public static boolean isBlockEmpty(BlockPos pos) {
        if (!NONSOLID_BLOCKS.contains(mc.world.getBlockState(pos).getBlock()))
            return false;

        Box box = new Box(pos);
        for (Entity e : mc.world.getEntities()) {
            if (e instanceof LivingEntity && box.intersects(e.getBoundingBox()))
                return false;
        }

        return true;
    }

    public static boolean canPlaceBlock(BlockPos pos) {
        if (pos.getY() < 0 || pos.getY() > 255 || !isBlockEmpty(pos))
            return false;

        for (Direction d : Direction.values()) {
            if ((d == Direction.DOWN && pos.getY() == 0) || (d == Direction.UP && pos.getY() == 255)
                    || NONSOLID_BLOCKS.contains(mc.world.getBlockState(pos.offset(d)).getBlock())
                    || mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0).distanceTo(
                    new Vec3d(pos.getX() + 0.5 + d.getOffsetX() * 0.5,
                            pos.getY() + 0.5 + d.getOffsetY() * 0.5,
                            pos.getZ() + 0.5 + d.getOffsetZ() * 0.5)) > 4.55)
                continue;

            return true;
        }
        return false;
    }

    public static void facePosAuto(double x, double y, double z, SettingRotate sr) {
        if (sr.getRotateMode() == 0)
            facePosPacket(x, y, z);
        else
            facePos(x, y, z);
    }

    public static void facePos(double x, double y, double z) {
        double diffX = x - mc.player.getX();
        double diffY = y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double diffZ = z - mc.player.getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        mc.player.yaw += MathHelper.wrapDegrees(yaw - mc.player.yaw);
        mc.player.pitch += MathHelper.wrapDegrees(pitch - mc.player.pitch);
    }

    public static void facePosPacket(double x, double y, double z) {
        double diffX = x - mc.player.getX();
        double diffY = y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double diffZ = z - mc.player.getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        //mc.player.headYaw = mc.player.yaw + MathHelper.wrapDegrees(yaw - mc.player.yaw);
        //mc.player.renderPitch = mc.player.pitch + MathHelper.wrapDegrees(pitch - mc.player.pitch);
        mc.player.networkHandler.sendPacket(
                new PlayerMoveC2SPacket.LookOnly(
                        mc.player.yaw + MathHelper.wrapDegrees(yaw - mc.player.yaw),
                        mc.player.pitch + MathHelper.wrapDegrees(pitch - mc.player.pitch), mc.player.isOnGround()));
    }
}