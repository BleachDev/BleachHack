package bleach.hack.utils.operation.blueprint;

import bleach.hack.utils.operation.Operation;
import bleach.hack.utils.operation.RemoveOperation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class RemoveOperationBlueprint extends OperationBlueprint {

	public RemoveOperationBlueprint(int localX, int localY, int localZ) {
		this.localX = localX;
		this.localY = localY;
		this.localZ = localZ;
	}

	@Override
	public Operation create(BlockPos pos, Direction dir) {
		return new RemoveOperation(pos.add(
				(dir == Direction.EAST ? localX : dir == Direction.WEST ? -localX : dir == Direction.SOUTH ? -localZ : localZ),
				localY,
				(dir == Direction.EAST ? localZ : dir == Direction.WEST ? -localZ : dir == Direction.SOUTH ? localX : -localX)));
	}
}