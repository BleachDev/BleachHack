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
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoBedrockBreak extends Module {

    int ticksPassed;
    boolean enabled = false;
    boolean active = false;
    Item pistonType;
    BlockPos pistonPos;
    BlockPos lookingCoords;
    BlockPos coords;
    String direction;

    public AutoBedrockBreak() {
        super("AutoBedrockBreak", KEY_UNBOUND, Category.EXPLOITS, "automatically breaks bedrock (IN DEVELOPMENT)",
                new SettingMode(" Piston Type", "Piston", "Sticky Piston"),
                new SettingToggle("Debug", true),
                new SettingMode("Structure Type", "Obsidian", "Cobblestone", "Iron Block")
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();
        assert mc.player != null;
        enabled = true;
        active = false;
        ticksPassed = 0;
        if (mc.player == null) {
            super.setToggled(false);
            return;
        }

        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {return;}
        lookingCoords = mc.crosshairTarget.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) mc.crosshairTarget).getBlockPos() : null;
        direction = mc.player.getHorizontalFacing().getName();
        switch(direction) {
            case "west":
                coords = lookingCoords.north().east().up();
                break;
            case "east":
                coords = lookingCoords.south().west().up();
                break;
            case "north":
                coords = lookingCoords.east().south().up();
                break;
            case "south":
                coords = lookingCoords.west().north().up();
                break;
        }
        //BleachLogger.infoMessage(lookingCoords.toString() + "||" + coords);
    }

    @Subscribe
    public void onTick(EventTick event) {
        assert mc.player != null;
        assert mc.interactionManager != null;
        assert mc.world != null;
        ticksPassed++;
        if (!enabled) return;


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
            switch(this.getSetting(2).asMode().mode) {
                case 0:
                    CrystalUtils.changeHotbarSlotToItem(Items.OBSIDIAN);
                    break;
                case 1:
                    CrystalUtils.changeHotbarSlotToItem(Items.COBBLESTONE);
                    break;
                case 2:
                    CrystalUtils.changeHotbarSlotToItem(Items.IRON_BLOCK);
                    break;
            }
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
            switch(this.getSetting(2).asMode().mode) {
                case 0:
                    CrystalUtils.changeHotbarSlotToItem(Items.NETHERRACK);
                    break;
                case 1:
                    CrystalUtils.changeHotbarSlotToItem(Items.COBBLESTONE);
                    break;
                case 2:
                    CrystalUtils.changeHotbarSlotToItem(Items.IRON_BLOCK);
                    break;
            }
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
        if (ticksPassed == 7) {
            switch(direction) {
                case "west":
                    mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
                            new BlockHitResult(mc.player.getPos(), Direction.UP, new BlockPos(coords.getX()-1, coords.getY()+1, coords.getZ()-1), true));
                    break;
                case "east":
                    mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
                            new BlockHitResult(mc.player.getPos(), Direction.UP, new BlockPos(coords.getX()+1, coords.getY()+1, coords.getZ()+1), true));
                    break;
                case "north":
                    mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
                            new BlockHitResult(mc.player.getPos(), Direction.UP, new BlockPos(coords.up().north().east()), true));
                    break;
                case "south":
                    mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
                            new BlockHitResult(mc.player.getPos(), Direction.UP, new BlockPos(coords.getX()-1, coords.getY()+1, coords.getZ()+1), true));
                    break;
            }
        }
        if (ticksPassed > 60 && enabled) {
            //if (
            //        mc.world.getBlockState(pistonPos.down(1)).getBlock() == Blocks.BEDROCK &&
            //                mc.world.getBlockState(pistonPos).getEntries().toString().contains("DirectionProperty{name=facing, clazz=class net.minecraft.util.math.Direction, values=[north, east, south, west, up, down]}=down")
            //) {
            //    BleachLogger.infoMessage("FAILED TO BREAK BEDROCK.");
            //    enabled = false;
            //    ticksPassed = 0;
            //    active = false;
            //    super.setToggled(false);
            //}
            if (mc.world.getBlockState(pistonPos.down(1)).getBlock() == Blocks.BEDROCK) {
                CrystalUtils.changeHotbarSlotToItem(pistonType);
                switch(direction) {
                    case "west":
                        WorldUtils.facePosPacket(mc.player.getX(), mc.player.getY() + 2, mc.player.getZ());
                        CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY(), coords.getZ()+1), Hand.MAIN_HAND, Direction.DOWN);
                        break;
                    case "east":
                        WorldUtils.facePosPacket(mc.player.getX(), mc.player.getY() + 2, mc.player.getZ());
                        CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY(), coords.getZ()-1), Hand.MAIN_HAND, Direction.DOWN);
                        break;
                    case "north":
                        WorldUtils.facePosPacket(mc.player.getX(), mc.player.getY() + 2, mc.player.getZ());
                        CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY(), coords.getZ()-1), Hand.MAIN_HAND, Direction.DOWN);
                        break;
                    case "south":
                        WorldUtils.facePosPacket(mc.player.getX(), mc.player.getY() + 2, mc.player.getZ());
                        CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY(), coords.getZ()+1), Hand.MAIN_HAND, Direction.DOWN);
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
    }
}
