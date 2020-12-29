package bleach.hack.utils.operation.blueprint;

import bleach.hack.utils.operation.Operation;
import bleach.hack.utils.operation.PlaceOperation;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PlaceOperationBlueprint extends OperationBlueprint {

	protected Item item;

	public PlaceOperationBlueprint(int localX, int localY, int localZ, Item item) {
		this.localX = localX;
		this.localY = localY;
		this.localZ = localZ;
		this.item = item;
	}

	@Override
	public Operation create(BlockPos pos, Direction dir) {
		return new PlaceOperation(pos.add(
				(dir == Direction.EAST ? localX : dir == Direction.WEST ? -localX : dir == Direction.SOUTH ? -localZ : localZ),
				localY,
				(dir == Direction.EAST ? localZ : dir == Direction.WEST ? -localZ : dir == Direction.SOUTH ? localX : -localX)), item);
	}
}