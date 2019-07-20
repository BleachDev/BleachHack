package bleach.hack.utils;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class WorldUtils {

	public static boolean doesAABBTouchBlock(AxisAlignedBB aabb, Block block) {
		for(int x = (int) Math.floor(aabb.minX); x < Math.ceil(aabb.maxX); x++) {
			for(int y = (int) Math.floor(aabb.minY); y < Math.ceil(aabb.maxY); y++) {
				for(int z = (int) Math.floor(aabb.minZ); z < Math.ceil(aabb.maxZ); z++) {
					if(Minecraft.getInstance().world.getBlockState(new BlockPos(x, y, z)).getBlock() == block) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
