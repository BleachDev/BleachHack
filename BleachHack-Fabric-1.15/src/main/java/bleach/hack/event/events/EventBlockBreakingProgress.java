package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class EventBlockBreakingProgress extends Event {
    private final BlockPos blockPos;
    private final Direction direction;

    public EventBlockBreakingProgress(BlockPos blockPos, Direction direction) {
        this.blockPos = blockPos;
        this.direction = direction;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public Direction getDirection() {
        return direction;
    }

}
