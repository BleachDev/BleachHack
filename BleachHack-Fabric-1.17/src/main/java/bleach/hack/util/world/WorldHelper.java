package bleach.hack.util.world;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.chunk.WorldChunk;

public class WorldHelper {

	private static MinecraftClient mc = MinecraftClient.getInstance();

	private static ClientWorld getWorld() {
		return mc.world;
	}
	
	public static List<WorldChunk> getLoadedChunks() {
		ClientWorld world = getWorld();
		
		List<WorldChunk> chunks = new ArrayList<>();

		if (world != null) {
			int viewDist = MinecraftClient.getInstance().options.viewDistance;
			
			for (int x = -viewDist; x <= viewDist; x++) {
				for (int z = -viewDist; z <= viewDist; z++) {
					WorldChunk chunk = world.getChunkManager().getWorldChunk((int) mc.player.getX() / 16 + x, (int) mc.player.getZ() / 16 + z);

					if (chunk != null) {
						chunks.add(chunk);
					}
				}
			}
		}

		return chunks;
	}
	
	public static List<BlockEntity> getBlockEntities() {
		List<BlockEntity> list = new ArrayList<>();
		getLoadedChunks().forEach(c -> list.addAll(c.getBlockEntities().values()));
		
		return list;
	}
}
