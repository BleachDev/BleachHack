package bleach.hack.epearledition.module.mods;

import bleach.hack.epearledition.event.events.EventTick;
import bleach.hack.epearledition.module.Category;
import bleach.hack.epearledition.module.Module;
import bleach.hack.epearledition.module.ModuleManager;
import bleach.hack.epearledition.setting.base.SettingMode;
import bleach.hack.epearledition.setting.base.SettingToggle;
import bleach.hack.epearledition.utils.BleachLogger;
import bleach.hack.epearledition.utils.CrystalUtils;
import bleach.hack.epearledition.utils.WorldUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class MultiAutoBedrockBreak extends Module {

    int ticksPassed;
    boolean enabled = false;
    boolean active;
    Item pistonType;
    BlockPos pistonPos;
    BlockPos targetCoords;
    boolean enableCrystalAura;

    // TODO: make sufficient blocks check
    public MultiAutoBedrockBreak() {
        super("MultiAutoBedrockBreak", KEY_UNBOUND, Category.EXPLOITS, "automatically breaks multiple bedrock (IN DEVELOPMENT)",
                new SettingMode("Piston Type", "Piston", "Sticky Piston"),
                new SettingToggle("Crystal Aura Toggle", false)
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();
        assert mc.player != null;
        enabled = true;
        active = false;
        enableCrystalAura = false;
        ticksPassed = 0;
        if (mc.player == null) {
            super.setToggled(false);
            return;
        }

        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) {return;}
        targetCoords = mc.crosshairTarget.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) mc.crosshairTarget).getEntity().getBlockPos() : null;
    }

    @Subscribe
    public void onTick(EventTick event) {
        assert mc.player != null;
        assert mc.interactionManager != null;
        assert mc.world != null;
        ticksPassed++;
        if (!enabled) return;

        if (this.getSetting(1).asToggle().state == true && ModuleManager.getModule(AutoCrystal.class).isToggled()) {
            ModuleManager.getModule(AutoCrystal.class).setToggled(false);
            enableCrystalAura = true;
        }


        switch(this.getSetting(0).asMode().mode) {
            case 0:
                pistonType = Items.PISTON;
                break;
            case 1:
                pistonType = Items.STICKY_PISTON;
                break;
        }


        if (ticksPassed == 1) {
            CrystalUtils.changeHotbarSlotToItem(Items.IRON_BLOCK);
            if (this.mc.world.getBlockState(targetCoords.east().south()).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.east().south(), -1, false, false);
            }
        }
        if (ticksPassed == 3) {
            if (this.mc.world.getBlockState(targetCoords.west().north()).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.west().north(), -1, false, false);
            }
        }
        if (ticksPassed == 5) {
            active = true;
            if (this.mc.world.getBlockState(targetCoords.up(1).east().south()).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.up(1).east().south(), -1, false, false);
            }
        }
        if (ticksPassed == 7) {
            if (this.mc.world.getBlockState(targetCoords.up(1).west().north()).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.up(1).west().north(), -1, false, false);
            }
        }
        if (ticksPassed == 9) {
            if (this.mc.world.getBlockState(targetCoords.up(2).east().south()).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.up(2).east().south(), -1, false, false);
            }
        }
        if (ticksPassed == 11) {
            if (this.mc.world.getBlockState(targetCoords.up(2).west().north()).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.up(2).west().north(), -1, false, false);
            }
        }
        if (ticksPassed == 13) {
            CrystalUtils.changeHotbarSlotToItem(Items.TNT);
            if (this.mc.world.getBlockState(targetCoords.up(2).north().east(2)).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.up(2).north().east(2), -1, false, false);
            }
            if (this.mc.world.getBlockState(targetCoords.up(2).east().south(2)).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.up(2).east().south(2), -1, false, false);
            }
        }
        if (ticksPassed == 15) {
            if (this.mc.world.getBlockState(targetCoords.up(2).south().west(2)).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.up(2).south().west(2), -1, false, false);
            }
            if (this.mc.world.getBlockState(targetCoords.up(2).west().north(2)).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.up(2).west().north(2), -1, false, false);
            }
        }
        if (ticksPassed == 17) {
            CrystalUtils.changeHotbarSlotToItem(Items.LEVER);
            if (this.mc.world.getBlockState(targetCoords.up(3).north().east()).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.up(3).north().east(), -1, false, false);
            }
            if (this.mc.world.getBlockState(targetCoords.up(3).east().south()).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.up(3).east().south(), -1, false, false);
            }
        }
        if (ticksPassed == 19) {
            if (this.mc.world.getBlockState(targetCoords.up(3).south().west()).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.up(3).south().west(), -1, false, false);
            }
            if (this.mc.world.getBlockState(targetCoords.up(3).west().north()).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.up(3).west().north(), -1, false, false);
            }
        }
        if (ticksPassed == 21) {
            CrystalUtils.changeHotbarSlotToItem(pistonType);
            WorldUtils.facePosPacket(mc.player.getX(), mc.player.getY() - 2, mc.player.getZ());
            CrystalUtils.placeBlock(new Vec3d(targetCoords.getX()+1, targetCoords.getY()+1, targetCoords.getZ()), Hand.MAIN_HAND, Direction.UP);
            CrystalUtils.placeBlock(new Vec3d(targetCoords.getX()-1, targetCoords.getY()+1, targetCoords.getZ()), Hand.MAIN_HAND, Direction.UP);
        }
        if (ticksPassed == 23) {
            WorldUtils.facePosPacket(mc.player.getX(), mc.player.getY() - 2, mc.player.getZ());
            CrystalUtils.placeBlock(new Vec3d(targetCoords.getX(), targetCoords.getY()+1, targetCoords.getZ()+1), Hand.MAIN_HAND, Direction.UP);
            CrystalUtils.placeBlock(new Vec3d(targetCoords.getX(), targetCoords.getY()+1, targetCoords.getZ()-1), Hand.MAIN_HAND, Direction.UP);
        }
        if (ticksPassed == 25) {
            CrystalUtils.changeHotbarSlotToItem(Items.TNT);
            if (this.mc.world.getBlockState(targetCoords.up(2).east()).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.up(2).east(), -1, false, false);
            }
            if (this.mc.world.getBlockState(targetCoords.up(2).west()).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.up(2).west(), -1, false, false);
            }
            if (this.mc.world.getBlockState(targetCoords.up(2).north()).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.up(2).north(), -1, false, false);
            }
            if (this.mc.world.getBlockState(targetCoords.up(2).south()).getBlock() == Blocks.AIR) {
                WorldUtils.placeBlock(targetCoords.up(2).south(), -1, false, false);
            }
        }
        if (ticksPassed == 29) {
            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
                    new BlockHitResult(mc.player.getPos(), Direction.UP, new BlockPos(targetCoords.getX()+1, targetCoords.getY()+3, targetCoords.getZ()+1), true));
            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
                    new BlockHitResult(mc.player.getPos(), Direction.UP, new BlockPos(targetCoords.getX()-1, targetCoords.getY()+3, targetCoords.getZ()-1), true));
        }
        if (ticksPassed > 100 && enabled) {
                CrystalUtils.changeHotbarSlotToItem(pistonType);
                WorldUtils.facePosPacket(mc.player.getX(), mc.player.getY() + 2, mc.player.getZ());
                CrystalUtils.placeBlock(new Vec3d(targetCoords.getX()+1, targetCoords.getY()+1, targetCoords.getZ()), Hand.MAIN_HAND, Direction.DOWN);
                CrystalUtils.placeBlock(new Vec3d(targetCoords.getX()-1, targetCoords.getY()+1, targetCoords.getZ()), Hand.MAIN_HAND, Direction.DOWN);
                CrystalUtils.placeBlock(new Vec3d(targetCoords.getX(), targetCoords.getY()+1, targetCoords.getZ()+1), Hand.MAIN_HAND, Direction.DOWN);
                CrystalUtils.placeBlock(new Vec3d(targetCoords.getX(), targetCoords.getY()+1, targetCoords.getZ()-1), Hand.MAIN_HAND, Direction.DOWN);
        }
        if (
                mc.world.getBlockState(targetCoords.north()).getBlock() == Blocks.PISTON ||
                mc.world.getBlockState(targetCoords.east()).getBlock() == Blocks.PISTON ||
                mc.world.getBlockState(targetCoords.south()).getBlock() == Blocks.PISTON ||
                mc.world.getBlockState(targetCoords.west()).getBlock() == Blocks.PISTON ||
                mc.world.getBlockState(targetCoords.north()).getBlock() == Blocks.AIR ||
                mc.world.getBlockState(targetCoords.east()).getBlock() == Blocks.AIR ||
                mc.world.getBlockState(targetCoords.south()).getBlock() == Blocks.AIR ||
                mc.world.getBlockState(targetCoords.west()).getBlock() == Blocks.AIR
        ) {
            BleachLogger.infoMessage("SUCCESSFULLY BROKE BEDROCK"+" TICKS PASSED: "+ticksPassed);
            if (this.getSetting(1).asToggle().state && enableCrystalAura) {
                ModuleManager.getModule(AutoCrystal.class).setToggled(true);
                enableCrystalAura = false;
            }
            enabled = false;
            ticksPassed = 0;
            active = false;
            super.setToggled(false);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (this.getSetting(1).asToggle().state && enableCrystalAura) {
            ModuleManager.getModule(AutoCrystal.class).setToggled(true);
            enableCrystalAura = false;
        }
    }
}
