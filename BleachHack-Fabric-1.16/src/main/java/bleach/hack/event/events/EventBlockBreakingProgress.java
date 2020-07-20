package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class EventBlockBreakingProgress extends Event {
	private final BlockPos pos;
	private final Direction direction;

	public EventBlockBreakingProgress(BlockPos pos, Direction direction) {
		this.pos = pos;
		this.direction = direction;
	}

	public BlockPos getPos() {
		return pos;
	}

	public Direction getDirection() {
		return direction;
	}

}
