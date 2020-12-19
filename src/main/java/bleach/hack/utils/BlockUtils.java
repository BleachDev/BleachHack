package bleach.hack.utils;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static net.minecraft.util.Hand.MAIN_HAND;
import static net.minecraft.util.math.Direction.UP;

public class BlockUtils
{
    private static MinecraftClient mc = Wrapper.getMinecraft();

    private static BlockPos _currBlock = null;
    private static boolean _started = false;

    public static void SetCurrentBlock(BlockPos block)
    {
        _currBlock = block;
        _started = false;
    }

    public static BlockPos GetCurrBlock()
    {
        return _currBlock;
    }

    public static boolean GetState()
    {
        if (_currBlock != null)
            return IsDoneBreaking(mc.world.getBlockState(_currBlock));

        return false;
    }

    private static boolean IsDoneBreaking(BlockState blockState)
    {
        return blockState.getBlock() == Blocks.BEDROCK
                || blockState.getBlock() == Blocks.AIR
                || blockState.getBlock() == Blocks.WATER
                || blockState.getBlock() == Blocks.LAVA;
    }

    public static boolean Update(float range, boolean rayTrace)
    {
        if (_currBlock == null)
            return false;

        BlockState state = mc.world.getBlockState(_currBlock);

        if (IsDoneBreaking(state) || mc.player.squaredDistanceTo(_currBlock.getX(), _currBlock.getY(), _currBlock.getZ()) > Math.pow(range, range))
        {
            _currBlock = null;
            return false;
        }

        // CPacketAnimation
        mc.player.swingHand(MAIN_HAND);

        Direction facing = UP;

        if (!_started)
        {
            _started = true;
            // Start Break

            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, _currBlock, facing));
        }
        else
        {
            mc.interactionManager.attackBlock(_currBlock, facing);
        }

        return true;
    }

}
