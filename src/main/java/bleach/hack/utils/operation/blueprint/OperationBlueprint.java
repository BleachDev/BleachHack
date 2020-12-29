package bleach.hack.utils.operation.blueprint;

import bleach.hack.utils.operation.Operation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class OperationBlueprint {

	/* popbob penis
	 *
	 *   |    *
	 *   |    *
	 *   |   * *
	 *  x|0,0______
	 *    z
	 */
	protected int localX;
	protected int localY;
	protected int localZ;

	public abstract Operation create(BlockPos pos, Direction dir);
}