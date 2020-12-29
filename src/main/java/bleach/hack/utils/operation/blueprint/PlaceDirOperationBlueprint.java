package bleach.hack.utils.operation.blueprint;

import bleach.hack.utils.operation.Operation;
import bleach.hack.utils.operation.PlaceDirOperation;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class PlaceDirOperationBlueprint extends PlaceOperationBlueprint {

	private Direction rotDir;

	public PlaceDirOperationBlueprint(int localX, int localY, int localZ, Item item, Direction localDir) {
		super(localX, localY, localZ, item);
		this.rotDir = localDir;
	}

	@Override
	public Operation create(BlockPos pos, Direction dir) {
		return new PlaceDirOperation(pos.add(
				(dir == Direction.EAST ? localX : dir == Direction.WEST ? -localX : dir == Direction.SOUTH ? -localZ : localZ),
				localY,
				(dir == Direction.EAST ? localZ : dir == Direction.WEST ? -localZ : dir == Direction.SOUTH ? localX : -localX)), item,
				rotDir.getAxis() == Axis.Y ? rotDir : rotDir == Direction.EAST ? dir : rotDir == Direction.SOUTH ? dir.rotateYClockwise()
						: rotDir == Direction.WEST ? dir.getOpposite() : dir.rotateYCounterclockwise());
	}
}