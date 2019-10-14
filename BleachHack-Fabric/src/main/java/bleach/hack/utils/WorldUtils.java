package bleach.hack.utils;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

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
		
	public static boolean isFluid(BlockPos pos) {
		List<Material> fluids = Arrays.asList(Material.WATER, Material.LAVA, Material.SEAGRASS);

        return fluids.contains(MinecraftClient.getInstance().world.getBlockState(pos).getMaterial());
    }
	
	public static boolean doesBoxTouchBlock(Box box, Block block) {
		for(int x = (int) Math.floor(box.minX); x < Math.ceil(box.maxX); x++) {
			for(int y = (int) Math.floor(box.minY); y < Math.ceil(box.maxY); y++) {
				for(int z = (int) Math.floor(box.minZ); z < Math.ceil(box.maxZ); z++) {
					if(MinecraftClient.getInstance().world.getBlockState(new BlockPos(x, y, z)).getBlock() == block) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean isBoxEmpty(Box box) {
		for(int x = (int) Math.floor(box.minX); x < Math.ceil(box.maxX); x++) {
			for(int y = (int) Math.floor(box.minY); y < Math.ceil(box.maxY); y++) {
				for(int z = (int) Math.floor(box.minZ); z < Math.ceil(box.maxZ); z++) {
					if(!NONSOLID_BLOCKS.contains(MinecraftClient.getInstance().world.getBlockState(new BlockPos(x, y, z)).getBlock())) {
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
}
