package bleach.hack.utils;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;

public class EntityUtils {

	public static List<Entity> getLoadedEntities() {
		return Minecraft.getInstance().world.getEntitiesWithinAABBExcludingEntity(
				null, new AxisAlignedBB(
						Minecraft.getInstance().player.posX - 128, 0,
						Minecraft.getInstance().player.posZ - 128,
						Minecraft.getInstance().player.posX + 128, 256,
						Minecraft.getInstance().player.posZ + 128));
	}
}
