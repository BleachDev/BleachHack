package bleach.hack.module.mods;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.CrystalUtils;
import bleach.hack.utils.WorldUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoBedrockBreak extends Module {

    int ticksPassed;
    boolean enabled = false;
    boolean active = false;
    Item pistonType;
    BlockPos pistonPos;
    BlockPos coords;
    String direction;

    public AutoBedrockBreak() {
        super("AutoBedrockBreak", KEY_UNBOUND, Category.EXPLOITS, "automatically breaks bedrock (IN DEVELOPMENT)",
                new SettingMode("Type", "Piston", "Sticky Piston"),
                new SettingToggle("Debug", true));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        assert mc.player != null;
        enabled = true;
        active = false;
        ticksPassed = 0;
        //super.setToggled(false);
    }

    @Subscribe
    public void onTick(EventTick event) {
        assert mc.player != null;
        assert mc.interactionManager != null;
        assert mc.world != null;
        ticksPassed++;
        if (!enabled) return;

        direction = mc.player.getHorizontalFacing().getName();
        coords = mc.player.getBlockPos();

        switch(this.getSetting(0).asMode().mode) {
            case 0:
                pistonType = Items.PISTON;
                break;
            case 1:
                pistonType = Items.STICKY_PISTON;
                break;
        }
        switch(direction) {
            case "west":
                pistonPos = new BlockPos(coords.getX()-1, coords.getY(), coords.getZ()+1);
                break;
            case "east":
                pistonPos = new BlockPos(coords.getX()+1, coords.getY(), coords.getZ()-1);
                break;
            case "north":
                pistonPos = new BlockPos(coords.getX()-1, coords.getY(), coords.getZ()-1);
                break;
            case "south":
                pistonPos = new BlockPos(coords.getX()+1, coords.getY(), coords.getZ()+1);
                break;
        }
        if (mc.world.getBlockState(pistonPos.down(1)).getBlock() != Blocks.BEDROCK && !active) {
            BleachLogger.infoMessage("Could not detect bedrock block to align to.");
            enabled = false;
            ticksPassed = 0;
            super.setToggled(false);
            return;
        }



        if (ticksPassed == 1) {
            CrystalUtils.changeHotbarSlotToItem(Items.OBSIDIAN);
            active = true;
            switch(direction) {
                case "west":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY()-1, coords.getZ()), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "east":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY()-1, coords.getZ()), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "north":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY()-1, coords.getZ()-1), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "south":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY()-1, coords.getZ()+1), Hand.MAIN_HAND, Direction.UP);
                    break;
            }
        }
        if (ticksPassed == 2) {
            CrystalUtils.changeHotbarSlotToItem(Items.NETHERRACK);
            switch(direction) {
                case "west":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY(), coords.getZ()), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "east":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY(), coords.getZ()), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "north":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY(), coords.getZ()-1), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "south":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY(), coords.getZ()+1), Hand.MAIN_HAND, Direction.UP);
                    break;
            }
        }
        if (ticksPassed == 3) {
            CrystalUtils.changeHotbarSlotToItem(Items.TNT);
            switch(direction) {
                case "west":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY(), coords.getZ()), Hand.MAIN_HAND, Direction.NORTH);
                    break;
                case "east":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY(), coords.getZ()), Hand.MAIN_HAND, Direction.SOUTH);
                    break;
                case "north":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY(), coords.getZ()-1), Hand.MAIN_HAND, Direction.EAST);
                    break;
                case "south":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY(), coords.getZ()+1), Hand.MAIN_HAND, Direction.WEST);
                    break;
            }
        }
        if (ticksPassed == 4) {
            CrystalUtils.changeHotbarSlotToItem(Items.LEVER);
            switch(direction) {
                case "west":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY()+1, coords.getZ()), Hand.MAIN_HAND, Direction.NORTH);
                    break;
                case "east":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY()+1, coords.getZ()), Hand.MAIN_HAND, Direction.SOUTH);
                    break;
                case "north":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY()+1, coords.getZ()-1), Hand.MAIN_HAND, Direction.EAST);
                    break;
                case "south":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY()+1, coords.getZ()+1), Hand.MAIN_HAND, Direction.WEST);
                    break;
            }
        }
        if (ticksPassed == 5) {
            CrystalUtils.changeHotbarSlotToItem(pistonType);
            switch(direction) {
                case "west":
                    WorldUtils.facePosPacket(coords.getX()-1, coords.getY()-1, coords.getZ()+1);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY()-1, coords.getZ()+1), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "east":
                    WorldUtils.facePosPacket(coords.getX()+1, coords.getY()-1, coords.getZ()-1);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY()-1, coords.getZ()-1), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "north":
                    WorldUtils.facePosPacket(coords.getX()-1, coords.getY()-1, coords.getZ()-1);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY()-1, coords.getZ()-1), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "south":
                    WorldUtils.facePosPacket(coords.getX()+1, coords.getY()-1, coords.getZ()+1);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY()-1, coords.getZ()+1), Hand.MAIN_HAND, Direction.UP);
                    break;
            }
        }
        if (ticksPassed == 6) {
            CrystalUtils.changeHotbarSlotToItem(Items.TNT);
            switch(direction) {
                case "west":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY(), coords.getZ()+1), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "east":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY(), coords.getZ()-1), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "north":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY(), coords.getZ()-1), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "south":
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY(), coords.getZ()+1), Hand.MAIN_HAND, Direction.UP);
                    break;
            }
        }
        //if (ticksPassed == 7) {
        //    switch(direction) {
        //        case "west":
        //            CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY(), coords.getZ()+1), Hand.MAIN_HAND, Direction.UP);
        //            break;
        //        case "east":
        //            CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY(), coords.getZ()-1), Hand.MAIN_HAND, Direction.UP);
        //            break;
        //        case "north":
        //            CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY(), coords.getZ()-1), Hand.MAIN_HAND, Direction.UP);
        //            break;
        //        case "south":
        //            CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY(), coords.getZ()+1), Hand.MAIN_HAND, Direction.UP);
        //            break;
        //    }
        //}
        if (ticksPassed > 60 && enabled) {
            if (mc.world.getBlockState(pistonPos.down(1)).getBlock() == Blocks.BEDROCK) {
                CrystalUtils.changeHotbarSlotToItem(pistonType);
                switch(direction) {
                    case "west":
                        WorldUtils.facePosPacket(mc.player.getX(), mc.player.getY() + 2, mc.player.getZ());
                        CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY(), coords.getZ()), Hand.MAIN_HAND, Direction.SOUTH);
                        break;
                    case "east":
                        WorldUtils.facePosPacket(mc.player.getX(), mc.player.getY() + 2, mc.player.getZ());
                        CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY(), coords.getZ()), Hand.MAIN_HAND, Direction.NORTH);
                        break;
                    case "north":
                        WorldUtils.facePosPacket(mc.player.getX(), mc.player.getY() + 2, mc.player.getZ());
                        CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY(), coords.getZ()-1), Hand.MAIN_HAND, Direction.WEST);
                        break;
                    case "south":
                        WorldUtils.facePosPacket(mc.player.getX(), mc.player.getY() + 2, mc.player.getZ());
                        CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY(), coords.getZ()+1), Hand.MAIN_HAND, Direction.EAST);
                        break;
                }
                if (getSetting(1).asToggle().state) {
                    BleachLogger.infoMessage("WAITING FOR PISTON TO BREAK! CURRENT BLOCKSTATE: "+mc.world.getBlockState(pistonPos).getBlock().toString());
                }
            } else {
                BleachLogger.infoMessage("SUCCESSFULLY BROKE BEDROCK");
                enabled = false;
                ticksPassed = 0;
                active = false;
                super.setToggled(false);
            }
        }
        // TODO: add flick lever and look upwards feature
    }
}
