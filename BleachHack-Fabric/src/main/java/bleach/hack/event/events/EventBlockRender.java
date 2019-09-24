package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class EventBlockRender extends Event {
	
    private BlockState blockState;
    private BlockPos blockPos;

    public EventBlockRender(BlockState blockState, BlockPos blockPos) {
        this.blockState = blockState;
        this.blockPos = blockPos;
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }
}
