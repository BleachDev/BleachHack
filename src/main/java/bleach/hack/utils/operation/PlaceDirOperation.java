package bleach.hack.utils.operation;

import bleach.hack.utils.WorldUtils;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * THIS DOES NOT PLACE A BLOCK ON A SPECIFIC SIDE OF A BLOCK!!
 * This faces you in a direction then places a block, useful for directional blocks like pistons
 */
public class PlaceDirOperation extends PlaceOperation {

	private Direction dir;
	private boolean faced = false;

	public PlaceDirOperation(BlockPos pos, Item item, Direction dir) {
		super(pos, item);
		this.dir = dir;
	}

	@Override
	public boolean execute() {
		for (int i = 0; i < 9; i++) {
			if (mc.player.inventory.getStack(i).getItem() == item) {
				if (WorldUtils.canPlaceBlock(pos)) {
					Vec3d lookPos = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0).add(dir.getOffsetX(), dir.getOffsetY(), dir.getOffsetZ());
					WorldUtils.facePosPacket(lookPos.getX(), lookPos.getY(), lookPos.getZ());

					if (!faced) {
						faced = true;
						return false;
					}

					return WorldUtils.placeBlock(pos, i, false, false);
				}
			}
		}

		return false;
	}

	@Override
	public boolean verify() {
		return true;
	}
}
