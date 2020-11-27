package bleach.hack.module.mods;

import bleach.hack.event.events.EventClientMove;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.EntityUtils;
import bleach.hack.utils.Timer;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.minecraft.util.Hand.MAIN_HAND;
import static net.minecraft.util.math.Direction.UP;

public class AutoTunnel extends Module
{
    public AutoTunnel()
    {
        super("AutoTunnel", KEY_UNBOUND, Category.WORLD, "Automines 2d tunnels.",
                new SettingMode("Blocks", "1x3", "2x3", "3x3", "Highway"),
                new SettingMode("Mode", "Packet", "Normal"),
                new SettingSlider("TimerForPause", 0, 10, 5, 1),
                new SettingToggle("PauseAutoWalk", true),
                new SettingToggle("UseTimerPause", false));
    }

    private List<BlockPos> _blocksToDestroy = new CopyOnWriteArrayList<>();
    private boolean _needPause = false;
    private Timer pauseTimer = new Timer();

    @Subscribe
    public void onMove(EventClientMove event)
    {
        _blocksToDestroy.clear();

        BlockPos playerPos = new BlockPos(Math.floor(mc.player.getX()), Math.floor(mc.player.getY()), Math.floor(mc.player.getZ()));

        switch (EntityUtils.GetFacing())
        {
            case East:
                switch (getSetting(0).asMode().mode)
                {
                    case 0:
                        for (int i = 0; i < 2; ++i)
                        {
                            _blocksToDestroy.add(playerPos.east());
                            _blocksToDestroy.add(playerPos.east().up());
                            _blocksToDestroy.add(playerPos.east().up().up());

                            playerPos = new BlockPos(playerPos).east();
                        }
                    case 1:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.east());
                            _blocksToDestroy.add(playerPos.east().up());
                            _blocksToDestroy.add(playerPos.east().up().up());
                            _blocksToDestroy.add(playerPos.east().north());
                            _blocksToDestroy.add(playerPos.east().north().up());
                            _blocksToDestroy.add(playerPos.east().north().up().up());

                            playerPos = new BlockPos(playerPos).east();
                        }
                        break;
                    case 2:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.east());
                            _blocksToDestroy.add(playerPos.east().up());
                            _blocksToDestroy.add(playerPos.east().up().up());
                            _blocksToDestroy.add(playerPos.east().north());
                            _blocksToDestroy.add(playerPos.east().north().up());
                            _blocksToDestroy.add(playerPos.east().north().up().up());
                            _blocksToDestroy.add(playerPos.east().north().north());
                            _blocksToDestroy.add(playerPos.east().north().north().up());
                            _blocksToDestroy.add(playerPos.east().north().north().up().up());

                            playerPos = new BlockPos(playerPos).east();
                        }
                        break;
                    case 3:
                        for (int i = 0; i < 4; ++i) {
                            _blocksToDestroy.add(playerPos.east());
                            _blocksToDestroy.add(playerPos.east().up());
                            _blocksToDestroy.add(playerPos.east().up(2));
                            _blocksToDestroy.add(playerPos.east().up(3));
                            _blocksToDestroy.add(playerPos.east().south());
                            _blocksToDestroy.add(playerPos.east().south().up());
                            _blocksToDestroy.add(playerPos.east().south().up(2));
                            _blocksToDestroy.add(playerPos.east().south().up(3));
                            _blocksToDestroy.add(playerPos.east().south(2).up());
                            _blocksToDestroy.add(playerPos.east().south(2).up(2));
                            _blocksToDestroy.add(playerPos.east().south(2).up(3));
                            _blocksToDestroy.add(playerPos.east().north());
                            _blocksToDestroy.add(playerPos.east().north().up());
                            _blocksToDestroy.add(playerPos.east().north().up(2));
                            _blocksToDestroy.add(playerPos.east().north().up(3));
                            _blocksToDestroy.add(playerPos.east().north(2));
                            _blocksToDestroy.add(playerPos.east().north(2).up());
                            _blocksToDestroy.add(playerPos.east().north(2).up(2));
                            _blocksToDestroy.add(playerPos.east().north(2).up(3));
                            _blocksToDestroy.add(playerPos.east().north(3).up());
                            _blocksToDestroy.add(playerPos.east().north(3).up(2));
                            _blocksToDestroy.add(playerPos.east().north(3).up(3));
                            playerPos = new BlockPos(playerPos).east();
                        }
                        break;
                    default:
                        break;
                }
                break;
            case North:
                switch (getSetting(0).asMode().mode)
                {
                    case 0:
                        for (int i = 0; i < 2; ++i)
                        {
                            _blocksToDestroy.add(playerPos.north());
                            _blocksToDestroy.add(playerPos.north().up());
                            _blocksToDestroy.add(playerPos.north().up().up());

                            playerPos = new BlockPos(playerPos).north();
                        }
                    case 1:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.north());
                            _blocksToDestroy.add(playerPos.north().up());
                            _blocksToDestroy.add(playerPos.north().up().up());
                            _blocksToDestroy.add(playerPos.north().east());
                            _blocksToDestroy.add(playerPos.north().east().up());
                            _blocksToDestroy.add(playerPos.north().east().up().up());

                            playerPos = new BlockPos(playerPos).north();
                        }
                        break;
                    case 2:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.north());
                            _blocksToDestroy.add(playerPos.north().up());
                            _blocksToDestroy.add(playerPos.north().up().up());
                            _blocksToDestroy.add(playerPos.north().east());
                            _blocksToDestroy.add(playerPos.north().east().up());
                            _blocksToDestroy.add(playerPos.north().east().up().up());
                            _blocksToDestroy.add(playerPos.north().east().east());
                            _blocksToDestroy.add(playerPos.north().east().east().up());
                            _blocksToDestroy.add(playerPos.north().east().east().up().up());

                            playerPos = new BlockPos(playerPos).north();
                        }
                        break;
                    case 3:
                        for (int i = 0; i < 4; ++i) {
                            _blocksToDestroy.add(playerPos.north());
                            _blocksToDestroy.add(playerPos.north().up());
                            _blocksToDestroy.add(playerPos.north().up(2));
                            _blocksToDestroy.add(playerPos.north().up(3));
                            _blocksToDestroy.add(playerPos.north().east());
                            _blocksToDestroy.add(playerPos.north().east().up());
                            _blocksToDestroy.add(playerPos.north().east().up(2));
                            _blocksToDestroy.add(playerPos.north().east().up(3));
                            _blocksToDestroy.add(playerPos.north().east(2).up());
                            _blocksToDestroy.add(playerPos.north().east(2).up(2));
                            _blocksToDestroy.add(playerPos.north().east(2).up(3));
                            _blocksToDestroy.add(playerPos.north().west());
                            _blocksToDestroy.add(playerPos.north().west().up());
                            _blocksToDestroy.add(playerPos.north().west().up(2));
                            _blocksToDestroy.add(playerPos.north().west().up(3));
                            _blocksToDestroy.add(playerPos.north().west(2));
                            _blocksToDestroy.add(playerPos.north().west(2).up());
                            _blocksToDestroy.add(playerPos.north().west(2).up(2));
                            _blocksToDestroy.add(playerPos.north().west(2).up(3));
                            _blocksToDestroy.add(playerPos.north().west(3).up());
                            _blocksToDestroy.add(playerPos.north().west(3).up(2));
                            _blocksToDestroy.add(playerPos.north().west(3).up(3));
                            playerPos = new BlockPos(playerPos).north();
                        }
                        break;
                    default:
                        break;
                }
                break;
            case South:
                switch (getSetting(0).asMode().mode)
                {
                    case 0:
                        for (int i = 0; i < 2; ++i)
                        {
                            _blocksToDestroy.add(playerPos.south());
                            _blocksToDestroy.add(playerPos.south().up());
                            _blocksToDestroy.add(playerPos.south().up().up());

                            playerPos = new BlockPos(playerPos).south();
                        }
                    case 1:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.south());
                            _blocksToDestroy.add(playerPos.south().up());
                            _blocksToDestroy.add(playerPos.south().up().up());
                            _blocksToDestroy.add(playerPos.south().west());
                            _blocksToDestroy.add(playerPos.south().west().up());
                            _blocksToDestroy.add(playerPos.south().west().up().up());

                            playerPos = new BlockPos(playerPos).south();
                        }
                        break;
                    case 2:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.south());
                            _blocksToDestroy.add(playerPos.south().up());
                            _blocksToDestroy.add(playerPos.south().up().up());
                            _blocksToDestroy.add(playerPos.south().west());
                            _blocksToDestroy.add(playerPos.south().west().up());
                            _blocksToDestroy.add(playerPos.south().west().up().up());
                            _blocksToDestroy.add(playerPos.south().west().west());
                            _blocksToDestroy.add(playerPos.south().west().west().up());
                            _blocksToDestroy.add(playerPos.south().west().west().up().up());

                            playerPos = new BlockPos(playerPos).south();
                        }
                        break;
                    case 3:
                        for (int i = 0; i < 4; ++i) {
                            _blocksToDestroy.add(playerPos.south());
                            _blocksToDestroy.add(playerPos.south().up());
                            _blocksToDestroy.add(playerPos.south().up(2));
                            _blocksToDestroy.add(playerPos.south().up(3));
                            _blocksToDestroy.add(playerPos.south().west());
                            _blocksToDestroy.add(playerPos.south().west().up());
                            _blocksToDestroy.add(playerPos.south().west().up(2));
                            _blocksToDestroy.add(playerPos.south().west().up(3));
                            _blocksToDestroy.add(playerPos.south().west(2).up());
                            _blocksToDestroy.add(playerPos.south().west(2).up(2));
                            _blocksToDestroy.add(playerPos.south().west(2).up(3));
                            _blocksToDestroy.add(playerPos.south().east());
                            _blocksToDestroy.add(playerPos.south().east().up());
                            _blocksToDestroy.add(playerPos.south().east().up(2));
                            _blocksToDestroy.add(playerPos.south().east().up(3));
                            _blocksToDestroy.add(playerPos.south().east(2));
                            _blocksToDestroy.add(playerPos.south().east(2).up());
                            _blocksToDestroy.add(playerPos.south().east(2).up(2));
                            _blocksToDestroy.add(playerPos.south().east(2).up(3));
                            _blocksToDestroy.add(playerPos.south().east(3).up());
                            _blocksToDestroy.add(playerPos.south().east(3).up(2));
                            _blocksToDestroy.add(playerPos.south().east(3).up(3));
                            playerPos = new BlockPos(playerPos).south();
                        }
                        break;
                    default:
                        break;
                }
                break;
            case West:
                switch (getSetting(0).asMode().mode)
                {
                    case 0:
                        for (int i = 0; i < 2; ++i)
                        {
                            _blocksToDestroy.add(playerPos.west());
                            _blocksToDestroy.add(playerPos.west().up());
                            _blocksToDestroy.add(playerPos.west().up().up());

                            playerPos = new BlockPos(playerPos).west();
                        }
                        break;
                    case 1:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.west());
                            _blocksToDestroy.add(playerPos.west().up());
                            _blocksToDestroy.add(playerPos.west().up().up());
                            _blocksToDestroy.add(playerPos.west().south());
                            _blocksToDestroy.add(playerPos.west().south().up());
                            _blocksToDestroy.add(playerPos.west().south().up().up());

                            playerPos = new BlockPos(playerPos).west();
                        }
                        break;
                    case 2:
                        for (int i = 0; i < 3; ++i)
                        {
                            _blocksToDestroy.add(playerPos.west());
                            _blocksToDestroy.add(playerPos.west().up());
                            _blocksToDestroy.add(playerPos.west().up().up());
                            _blocksToDestroy.add(playerPos.west().south());
                            _blocksToDestroy.add(playerPos.west().south().up());
                            _blocksToDestroy.add(playerPos.west().south().up().up());
                            _blocksToDestroy.add(playerPos.west().south().south());
                            _blocksToDestroy.add(playerPos.west().south().south().up());
                            _blocksToDestroy.add(playerPos.west().south().south().up().up());

                            playerPos = new BlockPos(playerPos).west();
                        }
                        break;
                    case 3:
                        for (int i = 0; i < 4; ++i) {
                            _blocksToDestroy.add(playerPos.west());
                            _blocksToDestroy.add(playerPos.west().up());
                            _blocksToDestroy.add(playerPos.west().up(2));
                            _blocksToDestroy.add(playerPos.west().up(3));
                            _blocksToDestroy.add(playerPos.west().north());
                            _blocksToDestroy.add(playerPos.west().north().up());
                            _blocksToDestroy.add(playerPos.west().north().up(2));
                            _blocksToDestroy.add(playerPos.west().north().up(3));
                            _blocksToDestroy.add(playerPos.west().north(2).up());
                            _blocksToDestroy.add(playerPos.west().north(2).up(2));
                            _blocksToDestroy.add(playerPos.west().north(2).up(3));
                            _blocksToDestroy.add(playerPos.west().south());
                            _blocksToDestroy.add(playerPos.west().south().up());
                            _blocksToDestroy.add(playerPos.west().south().up(2));
                            _blocksToDestroy.add(playerPos.west().south().up(3));
                            _blocksToDestroy.add(playerPos.west().south(2));
                            _blocksToDestroy.add(playerPos.west().south(2).up());
                            _blocksToDestroy.add(playerPos.west().south(2).up(2));
                            _blocksToDestroy.add(playerPos.west().south(2).up(3));
                            _blocksToDestroy.add(playerPos.west().south(3).up());
                            _blocksToDestroy.add(playerPos.west().south(3).up(2));
                            _blocksToDestroy.add(playerPos.west().south(3).up(3));
                            playerPos = new BlockPos(playerPos).west();
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }

        BlockPos toDestroy = null;

        for (BlockPos pos : _blocksToDestroy)
        {
            BlockState state = mc.world.getBlockState(pos);

            if (state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.NETHERRACK || state.getBlock() == Blocks.CAVE_AIR || state.getBlock() == Blocks.VOID_AIR)
                continue;

            toDestroy = pos;
            break;
        }

        if (toDestroy != null)
        {
//            event.setCancelled(true);

//            float[] rotations = EntityUtils.getLegitRotations(new Vec3d(toDestroy.getX(), toDestroy.getY(), toDestroy.getZ()));
//
//            mc.player.yaw = rotations[0];
//            mc.player.pitch = rotations[1];

            switch (getSetting(1).asMode().mode)
            {
                case 0:
                    mc.player.swingHand(MAIN_HAND);
                    mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                            PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, toDestroy, UP));
                    mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                            toDestroy, UP));
                    break;
                default:
                    break;
            }

            _needPause = true;
        }
        else
            _needPause = false;
    }

//    @Subscribe
//    public void onTick(EventTick event){
//        if (getSetting(4).asToggle().state) {
//            if (!_needPause && pauseTimer.passed(getSetting(2).asSlider().getValue() * 1000)) {
//                _needPause = true;
//                pauseTimer.reset();
//            }
//            if (_needPause && pauseTimer.passed(5500)) {
//                _needPause = false;
//                pauseTimer.reset();
//            }
//        }
//    }

    public boolean PauseAutoWalk()
    {
        return _needPause && getSetting(3).asToggle().state;
    }
}
