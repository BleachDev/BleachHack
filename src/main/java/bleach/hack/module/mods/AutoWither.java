package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.utils.BleachLogger;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoWither extends Module {

    public AutoWither() {
        super("AutoWither", KEY_UNBOUND, Category.PLAYER, "automatically air places anvil to surround");
    }

    @Subscribe
    public void onTick(EventTick event) {
        assert mc.player != null;
        assert mc.interactionManager != null;
        assert mc.world != null;
        int soulSandSlot = -1;
        int skullSlot = -1;
        for(int i = 0; i < 9; i++){
            if (mc.player.inventory.getStack(i).getItem() == Blocks.SOUL_SAND.asItem()){
                soulSandSlot = i;
                break;
            }
        }
        for(int i = 0; i < 9; i++){
            if (mc.player.inventory.getStack(i).getItem() == Blocks.WITHER_SKELETON_SKULL.asItem()){
                skullSlot = i;
                break;
            }
        }
        if (soulSandSlot == -1) {
            BleachLogger.warningMessage("No soul sand found in your hotbar!");
            ModuleManager.getModule(AutoWither.class).toggle();
            return;
        }
        if (skullSlot == -1) {
            BleachLogger.warningMessage("No wither skulls found in your hotbar!");
            ModuleManager.getModule(AutoWither.class).toggle();
            return;
        }
        int prevSlot = mc.player.inventory.selectedSlot;
        BlockPos targetPos = mc.player.getBlockPos();
        if(
            mc.world.getBlockState(targetPos.add(1, 0, 0)).getMaterial().isReplaceable() &&
            mc.world.getBlockState(targetPos.add(1, 1, 0)).getMaterial().isReplaceable() &&
            mc.world.getBlockState(targetPos.add(1, 1, 1)).getMaterial().isReplaceable() &&
            mc.world.getBlockState(targetPos.add(1, 1, -1)).getMaterial().isReplaceable() &&
            mc.world.getBlockState(targetPos.add(1, 2, 0)).getMaterial().isReplaceable() &&
            mc.world.getBlockState(targetPos.add(1, 2, 1)).getMaterial().isReplaceable() &&
            mc.world.getBlockState(targetPos.add(1, 2, -1)).getMaterial().isReplaceable()
        ){
            BleachLogger.warningMessage("Attempting to place wither");
            mc.player.inventory.selectedSlot = soulSandSlot;
            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos().add(1, 0, 0), Direction.UP, targetPos.add(1, 0, 0), false));
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos().add(1, 1, 0), Direction.UP, targetPos.add(1, 1, 0), false));
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos().add(1, 1, 1), Direction.UP, targetPos.add(1, 1, 1), false));
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos().add(1, 1, -1), Direction.UP, targetPos.add(1, 1, -1), false));
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.inventory.selectedSlot = skullSlot;
            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos().add(1, 2, 0), Direction.UP, targetPos.add(1, 2, 0), false));
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos().add(1, 2, 1), Direction.UP, targetPos.add(1, 2, 1), false));
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos().add(1, 2, -1), Direction.UP, targetPos.add(1, 2, -1), false));
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        mc.player.inventory.selectedSlot = prevSlot;
        ModuleManager.getModule(AutoWither.class).toggle();
    }
}
