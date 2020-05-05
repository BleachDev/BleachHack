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

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class WorldUtils {

	public static List<Block> NONSOLID_BLOCKS = Arrays.asList(
			Blocks.AIR, Blocks.LAVA, Blocks.SEAGRASS,
			Blocks.WATER, Blocks.VINE, Blocks.TALL_SEAGRASS,
			Blocks.SNOW, Blocks.TALL_GRASS, Blocks.FIRE, Blocks.GRASS);
	
	public static List<Block> RIGHTCLICKABLE_BLOCKS = Arrays.asList(
			Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST,
			Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX,
			Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX,
			Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX,
			Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX,
			Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX,
			Blocks.BLACK_SHULKER_BOX, Blocks.ACACIA_TRAPDOOR, Blocks.ANVIL,
			Blocks.OAK_BUTTON, Blocks.STONE_BUTTON, Blocks.BIRCH_TRAPDOOR,
			Blocks.DARK_OAK_TRAPDOOR, Blocks.REPEATER, Blocks.COMPARATOR,
			Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.BIRCH_FENCE_GATE,
			Blocks.JUNGLE_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE, Blocks.ACACIA_FENCE_GATE,
			Blocks.BREWING_STAND, Blocks.DISPENSER, Blocks.DROPPER,
			Blocks.LEVER, Blocks.NOTE_BLOCK, Blocks.JUKEBOX,
			Blocks.BEACON, Blocks.RED_BED, Blocks.JUNGLE_TRAPDOOR,
			Blocks.FURNACE, Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR,
			Blocks.BIRCH_DOOR, Blocks.JUNGLE_DOOR, Blocks.ACACIA_DOOR,
			Blocks.DARK_OAK_DOOR, Blocks.CAKE, Blocks.ENCHANTING_TABLE,
			Blocks.DRAGON_EGG, Blocks.HOPPER, Blocks.REPEATING_COMMAND_BLOCK,
			Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.CRAFTING_TABLE,
			Blocks.OAK_TRAPDOOR, Blocks.SPRUCE_TRAPDOOR, Blocks.BIRCH_BUTTON,
			Blocks.ACACIA_BUTTON, Blocks.DARK_OAK_BUTTON, Blocks.JUNGLE_BUTTON,
			Blocks.SPRUCE_BUTTON, Blocks.BARREL, Blocks.BLAST_FURNACE);
		
	public static boolean doesAABBTouchBlock(AxisAlignedBB aabb, Block block) {
		for (int x = (int) Math.floor(aabb.minX); x < Math.ceil(aabb.maxX); x++) {
			for (int y = (int) Math.floor(aabb.minY); y < Math.ceil(aabb.maxY); y++) {
				for (int z = (int) Math.floor(aabb.minZ); z < Math.ceil(aabb.maxZ); z++) {
					if (Minecraft.getInstance().world.getBlockState(new BlockPos(x, y, z)).getBlock() == block) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
