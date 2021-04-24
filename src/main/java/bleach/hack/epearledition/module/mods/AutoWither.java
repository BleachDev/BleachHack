//package bleach.hack.module.mods;
//
//import bleach.hack.event.events.EventTick;
//import bleach.hack.module.Category;
//import bleach.hack.module.Module;
//import bleach.hack.module.ModuleManager;
//import bleach.hack.setting.base.SettingToggle;
//import bleach.hack.utils.BleachLogger;
//import bleach.hack.utils.WorldUtils;
//import com.google.common.eventbus.Subscribe;
//import net.minecraft.block.Blocks;
//import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
//import net.minecraft.util.Hand;
//import net.minecraft.util.hit.BlockHitResult;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.Direction;
//import net.minecraft.util.math.Vec3d;
//
//public class AutoWither extends Module {
//    int ticksPassed;
//    int soulSandSlot = -1;
//    int skullSlot = -1;
//
//    public AutoWither() {
//        super("AutoWither", KEY_UNBOUND, Category.PLAYER, "automatically air places anvil to surround",
//                new SettingToggle("Center", false),
//                new SettingToggle("Debug", false)
//                );
//    }
//
//    @Override
//    public void onEnable() {
//        super.onEnable();
//        ticksPassed = 0;
//        if (getSetting(1).asToggle().state) {
//            BleachLogger.warningMessage("Setting ticks passed to 0");
//        }
//    }
//
//    @Subscribe
//    public void onTick(EventTick event) {
//        assert mc.player != null;
//        assert mc.interactionManager != null;
//        assert mc.world != null;
//        ticksPassed++;
//        for(int i = 0; i < 9; i++){
//            if (mc.player.inventory.getStack(i).getItem() == Blocks.SOUL_SAND.asItem()){
//                soulSandSlot = i;
//                break;
//            }
//        }
//        for(int i = 0; i < 9; i++){
//            if (mc.player.inventory.getStack(i).getItem() == Blocks.WITHER_SKELETON_SKULL.asItem()){
//                skullSlot = i;
//                break;
//            }
//        }
//        if (soulSandSlot == -1) {
//            BleachLogger.warningMessage("No soul sand found in your hotbar!");
//            ModuleManager.getModule(AutoWither.class).toggle();
//            return;
//        }
//        if (skullSlot == -1) {
//            BleachLogger.warningMessage("No wither skulls found in your hotbar!");
//            ModuleManager.getModule(AutoWither.class).toggle();
//            return;
//        }
//        int prevSlot = mc.player.inventory.selectedSlot;
//        BlockPos targetPos = mc.player.getBlockPos();
//        if (ticksPassed == 1) {
//            if(
//                //mc.world.getBlockState(targetPos.add(1, 0, 1)).getMaterial().toString().contains("AIR") &&
//                //mc.world.getBlockState(targetPos.add(1, 0, -1)).getMaterial().toString().contains("AIR") &&
//                    mc.world.getBlockState(targetPos.add(1, 0, 0)).getMaterial().isReplaceable() &&
//                            mc.world.getBlockState(targetPos.add(1, 1, 0)).getMaterial().isReplaceable() &&
//                            mc.world.getBlockState(targetPos.add(1, 1, 1)).getMaterial().isReplaceable() &&
//                            mc.world.getBlockState(targetPos.add(1, 1, -1)).getMaterial().isReplaceable() &&
//                            mc.world.getBlockState(targetPos.add(1, 2, 0)).getMaterial().isReplaceable() &&
//                            mc.world.getBlockState(targetPos.add(1, 2, 1)).getMaterial().isReplaceable() &&
//                            mc.world.getBlockState(targetPos.add(1, 2, -1)).getMaterial().isReplaceable()
//            ){
//                if (getSetting(1).asToggle().state) {
//                    BleachLogger.warningMessage("Attempting to place wither body");
//                }
//                mc.player.inventory.selectedSlot = soulSandSlot;
//                mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos().add(2, 0, 0), Direction.UP, targetPos.add(1, 0, 0), false));
//                mc.player.swingHand(Hand.MAIN_HAND);
//                mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos().add(2, 1, 0), Direction.UP, targetPos.add(1, 1, 0), false));
//                mc.player.swingHand(Hand.MAIN_HAND);
//                mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos().add(2, 1, 1), Direction.UP, targetPos.add(1, 1, 1), false));
//                mc.player.swingHand(Hand.MAIN_HAND);
//                mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos().add(2, 1, -1), Direction.UP, targetPos.add(1, 1, -1), false));
//                mc.player.swingHand(Hand.MAIN_HAND);
//            }
//        }
//        if (ticksPassed == 2) {
//            if(
//                mc.world.getBlockState(targetPos.add(2, 2, 0)).getMaterial().isReplaceable() &&
//                mc.world.getBlockState(targetPos.add(2, 2, 1)).getMaterial().isReplaceable() &&
//                mc.world.getBlockState(targetPos.add(2, 2, -1)).getMaterial().isReplaceable()
//            ) {
//                if (getSetting(1).asToggle().state) {
//                    BleachLogger.warningMessage("Attempting to place wither skulls");
//                }
//                mc.player.inventory.selectedSlot = skullSlot;
//                if (getSetting(0).asToggle().state) {
//                    Vec3d centerPos = Vec3d.of(mc.player.getBlockPos()).add(0.5, 0.5, 0.5);
//                    mc.player.updatePosition(centerPos.x, centerPos.y, centerPos.z);
//                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(centerPos.x, centerPos.y, centerPos.z, mc.player.isOnGround()));
//                }
//
//                WorldUtils.facePos(mc.player.getPos().x, mc.player.getPos().y - 2, mc.player.getPos().z);
//                WorldUtils.facePosPacket(mc.player.getPos().x, mc.player.getPos().y - 2, mc.player.getPos().z);
//
//                mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos().add(2, 1, 0), Direction.DOWN, targetPos.add(1, 2, 0), false));
//                mc.player.swingHand(Hand.MAIN_HAND);
//                mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos().add(2, 1, 1), Direction.DOWN, targetPos.add(1, 2, 1), false));
//                mc.player.swingHand(Hand.MAIN_HAND);
//                mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(mc.player.getPos().add(2, 1, -1), Direction.DOWN, targetPos.add(1, 2, -1), false));
//                mc.player.swingHand(Hand.MAIN_HAND);
//
//
//                WorldUtils.facePos(mc.player.getPos().x + 1, mc.player.getPos().y + 1, mc.player.getPos().z);
//                WorldUtils.facePosPacket(mc.player.getPos().x + 1, mc.player.getPos().y + 1, mc.player.getPos().z);
//            }
//            mc.player.inventory.selectedSlot = prevSlot;
//            ModuleManager.getModule(AutoWither.class).toggle();
//        }
//    }
//}
