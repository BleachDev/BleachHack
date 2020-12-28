package bleach.hack.module.mods;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.CrystalUtils;
import bleach.hack.utils.WorldUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoBedrockBreak extends Module {

    int ticksPassed;
    boolean enabled = false;
    Item pistonType;

    public AutoBedrockBreak() {
        super("AutoBedrockBreak", KEY_UNBOUND, Category.EXPLOITS, "automatically breaks bedrock (IN DEVELOPMENT)",
                new SettingMode("Type", "Piston", "Sticky Piston"));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        assert mc.player != null;
        enabled = true;
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
        String direction = mc.player.getHorizontalFacing().getName();
        BlockPos coords = mc.player.getBlockPos();

        switch(this.getSetting(0).asMode().mode) {
            case 0:
                pistonType = Items.PISTON;
                break;
            case 1:
                pistonType = Items.STICKY_PISTON;
                break;
        }

        if (ticksPassed == 1) {
            switch(direction) {
                case "west":
                    CrystalUtils.changeHotbarSlotToItem(Items.OBSIDIAN);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY()-1, coords.getZ()), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "east":
                    CrystalUtils.changeHotbarSlotToItem(Items.OBSIDIAN);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY()-1, coords.getZ()), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "north":
                    CrystalUtils.changeHotbarSlotToItem(Items.OBSIDIAN);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY()-1, coords.getZ()-1), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "south":
                    CrystalUtils.changeHotbarSlotToItem(Items.OBSIDIAN);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY()-1, coords.getZ()+1), Hand.MAIN_HAND, Direction.UP);
                    break;
            }
        }
        if (ticksPassed == 2) {
            switch(direction) {
                case "west":
                    CrystalUtils.changeHotbarSlotToItem(Items.NETHERRACK);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY(), coords.getZ()), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "east":
                    CrystalUtils.changeHotbarSlotToItem(Items.NETHERRACK);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY(), coords.getZ()), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "north":
                    CrystalUtils.changeHotbarSlotToItem(Items.NETHERRACK);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY(), coords.getZ()-1), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "south":
                    CrystalUtils.changeHotbarSlotToItem(Items.NETHERRACK);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY(), coords.getZ()+1), Hand.MAIN_HAND, Direction.UP);
                    break;
            }
        }
        if (ticksPassed == 3) {
            switch(direction) {
                case "west":
                    CrystalUtils.changeHotbarSlotToItem(Items.TNT);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY(), coords.getZ()), Hand.MAIN_HAND, Direction.NORTH);
                    break;
                case "east":
                    CrystalUtils.changeHotbarSlotToItem(Items.TNT);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY(), coords.getZ()), Hand.MAIN_HAND, Direction.SOUTH);
                    break;
                case "north":
                    CrystalUtils.changeHotbarSlotToItem(Items.TNT);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY(), coords.getZ()-1), Hand.MAIN_HAND, Direction.EAST);
                    break;
                case "south":
                    CrystalUtils.changeHotbarSlotToItem(Items.TNT);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY(), coords.getZ()+1), Hand.MAIN_HAND, Direction.WEST);
                    break;
            }
        }
        if (ticksPassed == 4) {
            switch(direction) {
                case "west":
                    CrystalUtils.changeHotbarSlotToItem(Items.LEVER);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY()+1, coords.getZ()), Hand.MAIN_HAND, Direction.NORTH);
                    break;
                case "east":
                    CrystalUtils.changeHotbarSlotToItem(Items.LEVER);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY()+1, coords.getZ()), Hand.MAIN_HAND, Direction.SOUTH);
                    break;
                case "north":
                    CrystalUtils.changeHotbarSlotToItem(Items.LEVER);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY()+1, coords.getZ()-1), Hand.MAIN_HAND, Direction.EAST);
                    break;
                case "south":
                    CrystalUtils.changeHotbarSlotToItem(Items.LEVER);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX(), coords.getY()+1, coords.getZ()+1), Hand.MAIN_HAND, Direction.WEST);
                    break;
            }
        }
        if (ticksPassed == 5) {
            switch(direction) {
                case "west":
                    CrystalUtils.changeHotbarSlotToItem(pistonType);
                    WorldUtils.facePosPacket(coords.getX()-1, coords.getY()-1, coords.getZ()+1);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY()-1, coords.getZ()+1), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "east":
                    CrystalUtils.changeHotbarSlotToItem(pistonType);
                    WorldUtils.facePosPacket(coords.getX()+1, coords.getY()-1, coords.getZ()-1);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY()-1, coords.getZ()-1), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "north":
                    CrystalUtils.changeHotbarSlotToItem(pistonType);
                    WorldUtils.facePosPacket(coords.getX()-1, coords.getY()-1, coords.getZ()-1);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY()-1, coords.getZ()-1), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "south":
                    CrystalUtils.changeHotbarSlotToItem(pistonType);
                    WorldUtils.facePosPacket(coords.getX()+1, coords.getY()-1, coords.getZ()+1);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY()-1, coords.getZ()+1), Hand.MAIN_HAND, Direction.UP);
                    break;
            }
        }
        if (ticksPassed == 6) {
            switch(direction) {
                case "west":
                    CrystalUtils.changeHotbarSlotToItem(Items.TNT);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY(), coords.getZ()+1), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "east":
                    CrystalUtils.changeHotbarSlotToItem(Items.TNT);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY(), coords.getZ()-1), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "north":
                    CrystalUtils.changeHotbarSlotToItem(Items.TNT);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()-1, coords.getY(), coords.getZ()-1), Hand.MAIN_HAND, Direction.UP);
                    break;
                case "south":
                    CrystalUtils.changeHotbarSlotToItem(Items.TNT);
                    CrystalUtils.placeBlock(new Vec3d(coords.getX()+1, coords.getY(), coords.getZ()+1), Hand.MAIN_HAND, Direction.UP);
                    break;
            }
            enabled = false;
            ticksPassed = 0;
            super.setToggled(false);
        }
    }
}
