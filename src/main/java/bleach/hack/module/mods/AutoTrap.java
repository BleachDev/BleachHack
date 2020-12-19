package bleach.hack.module.mods;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.setting.base.SettingMode;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoTrap extends Module {

    public AutoTrap() {
        super("AutoTrap", KEY_UNBOUND, Category.COMBAT, "autotraps other players near you, does not trap friends!",
                new SettingMode("Mode", "Top", "Full", "Bed Aura")
            );
    }

    @Subscribe
    public void onTick(EventTick event) {
        assert mc.player != null;
        assert mc.interactionManager != null;
        int blockSlot = -1;
        PlayerEntity target = null;
        for(int i = 0; i < 9; i++){
            if (mc.player.inventory.getStack(i).getItem() == Blocks.OBSIDIAN.asItem() || mc.player.inventory.getStack(i).getItem() == Blocks.NETHERITE_BLOCK.asItem()){
                blockSlot = i;
                break;
            }
        }
        if (blockSlot == -1) return;
        for(PlayerEntity player : mc.world.getPlayers()){
            if (player != mc.player && !BleachHack.friendMang.has(player.getDisplayName().getString()))
            if (target == null){
                target = player;
            }else if (mc.player.distanceTo(target) > mc.player.distanceTo(player)){
                target = player;
            }
        }
        if (target == null) return;
        if (target == mc.player) return;
        if (mc.player.distanceTo(target) < 5){
            int prevSlot = mc.player.inventory.selectedSlot;
            mc.player.inventory.selectedSlot = blockSlot;
            BlockPos targetPos = target.getBlockPos().up();
            switch (getSetting(0).asMode().mode) {
                case 0:
                    if(mc.world.getBlockState(targetPos.add(0, 1, 0)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(0, 1, 0), Direction.UP, targetPos.add(0, 1, 0), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if(mc.world.getBlockState(targetPos.add(1, 0, 0)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(1, 0, 0), Direction.UP, targetPos.add(1, 0, 0), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if(mc.world.getBlockState(targetPos.add(-1, 0, 0)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(-1, 0, 0), Direction.UP, targetPos.add(-1, 0, 0), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if(mc.world.getBlockState(targetPos.add(0, 0, 1)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(0, 0, 1), Direction.UP, targetPos.add(0, 0, 1), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if(mc.world.getBlockState(targetPos.add(0, 0, -1)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(0, 0, -1), Direction.UP, targetPos.add(0, 0, -1), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    break;
                case 1:
                    if(mc.world.getBlockState(targetPos.add(0, 1, 0)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(0, 1, 0), Direction.UP, targetPos.add(0, 1, 0), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if(mc.world.getBlockState(targetPos.add(1, 0, 0)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(1, 0, 0), Direction.UP, targetPos.add(1, 0, 0), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if(mc.world.getBlockState(targetPos.add(-1, 0, 0)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(-1, 0, 0), Direction.UP, targetPos.add(-1, 0, 0), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if(mc.world.getBlockState(targetPos.add(0, 0, 1)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(0, 0, 1), Direction.UP, targetPos.add(0, 0, 1), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if(mc.world.getBlockState(targetPos.add(0, 0, -1)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(0, 0, -1), Direction.UP, targetPos.add(0, 0, -1), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if(mc.world.getBlockState(targetPos.add(1, -1, 0)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(1, 0, 0), Direction.UP, targetPos.add(1, -1, 0), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if(mc.world.getBlockState(targetPos.add(-1, -1, 0)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(-1, 0, 0), Direction.UP, targetPos.add(-1, -1, 0), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if(mc.world.getBlockState(targetPos.add(0, -1, 1)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(0, 0, 1), Direction.UP, targetPos.add(0, -1, 1), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if(mc.world.getBlockState(targetPos.add(0, -1, -1)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(0, 0, -1), Direction.UP, targetPos.add(0, -1, -1), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    break;
                case 2:
                    if(mc.world.getBlockState(targetPos.add(0, 1, 0)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(0, 1, 0), Direction.UP, targetPos.add(0, 1, 0), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if(mc.world.getBlockState(targetPos.add(1, -1, 0)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(1, 0, 0), Direction.UP, targetPos.add(1, -1, 0), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if(mc.world.getBlockState(targetPos.add(-1, -1, 0)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(-1, 0, 0), Direction.UP, targetPos.add(-1, -1, 0), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if(mc.world.getBlockState(targetPos.add(0, -1, 1)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(0, 0, 1), Direction.UP, targetPos.add(0, -1, 1), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    if(mc.world.getBlockState(targetPos.add(0, -1, -1)).getMaterial().isReplaceable()){
                        mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(target.getPos().add(0, 0, -1), Direction.UP, targetPos.add(0, -1, -1), false));
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    break;
            }

            mc.player.inventory.selectedSlot = prevSlot;
            ModuleManager.getModule(AutoTrap.class).toggle();
        }
    }
}
