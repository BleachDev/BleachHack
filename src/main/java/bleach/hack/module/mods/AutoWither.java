//package bleach.hack.module.mods;
//
//import bleach.hack.BleachHack;
//import bleach.hack.event.events.EventTick;
//import bleach.hack.module.Category;
//import bleach.hack.module.Module;
//import bleach.hack.setting.base.SettingMode;
//import bleach.hack.setting.base.SettingSlider;
//import bleach.hack.setting.base.SettingToggle;
//import bleach.hack.utils.BleachLogger;
//import bleach.hack.utils.WorldUtils;
//import com.google.common.collect.Streams;
//import com.google.common.eventbus.Subscribe;
//import net.minecraft.block.Block;
//import net.minecraft.block.Blocks;
//import net.minecraft.block.ShulkerBoxBlock;
//import net.minecraft.client.gui.screen.ingame.Generic3x3ContainerScreen;
//import net.minecraft.client.gui.screen.ingame.HopperScreen;
//import net.minecraft.enchantment.EnchantmentHelper;
//import net.minecraft.enchantment.Enchantments;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.AirBlockItem;
//import net.minecraft.item.BlockItem;
//import net.minecraft.item.Item;
//import net.minecraft.screen.slot.SlotActionType;
//import net.minecraft.util.Hand;
//import net.minecraft.util.hit.BlockHitResult;
//import net.minecraft.util.hit.HitResult;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.Direction;
//import net.minecraft.util.math.Vec3d;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class AutoWither extends Module {
//
//    private BlockPos pos;
//
//    private int wither;
//    private int soulsand;
//    private int[] rot;
//    private float[] startRot;
//
//    private boolean active;
//    private boolean openedDispenser;
//    private int dispenserTicks;
//
//    private int ticksPassed;
//    private int timer = 0;
//
//    public AutoWither() {
//        super("AutoWither", KEY_UNBOUND, Category.COMBAT, "ching chong auto32k no skid 2020",
//                new SettingToggle("Legit Place", true),
//                new SettingToggle("Killaura", true),
//                new SettingSlider("CPS", 0, 20, 20, 0),
//                new SettingMode("CPS", "Clicks/Sec", "Clicks/Tick", "Tick Delay"),
//                new SettingToggle("Timeout", false),
//                new SettingMode("Place", "Auto", "Looking"));
//    }
//
//    public void onEnable() {
//        if (mc.world == null) return;
//
//        super.onEnable();
//
//        ticksPassed = 0;
//        wither = -1;
//        soulsand = -1;
//        active = false;
//        openedDispenser = false;
//        dispenserTicks = 0;
//        timer = 0;
//
//        for (int i = 0; i <= 2; i++) {
//            Item item = mc.player.inventory.getStack(i).getItem();
//            if (item == Item.fromBlock(Blocks.WITHER_SKELETON_SKULL)) wither = i;
//            else if (item == Item.fromBlock(Blocks.SOUL_SAND)) soulsand = i;
//        }
//
//        if (wither == -1) BleachLogger.errorMessage("Missing wither skulls");
//        else if (soulsand == -1) BleachLogger.errorMessage("Missing soul sand");
//
//        if (wither == -1 || soulsand == -1) {
//            setToggled(false);
//            return;
//        }
//
//        if (getSetting(5).asMode().mode == 1) {
//            HitResult ray = mc.player.rayTrace(5, mc.getTickDelta(), false);
//            pos = new BlockPos(ray.getPos()).up();
//
//            double x = pos.getX() - mc.player.getPos().x;
//            double z = pos.getZ() - mc.player.getPos().z;
//
//            rot = Math.abs(x) > Math.abs(z) ? x > 0 ? new int[]{-1, 0} : new int[]{1, 0} : z > 0 ? new int[]{0, -1} : new int[]{0, 1};
//
//            if (!(WorldUtils.canPlaceBlock(pos) /*|| canPlaceBlock(pos.add(rot[0], 0, rot[1]))*/)
//                    || !WorldUtils.isBlockEmpty(pos)
//                    || !WorldUtils.isBlockEmpty(pos.add(rot[0], 0, rot[1]))
//                    || !WorldUtils.isBlockEmpty(pos.add(0, 1, 0))
//                    || !WorldUtils.isBlockEmpty(pos.add(0, 2, 0))
//                    || !WorldUtils.isBlockEmpty(pos.add(rot[0], 1, rot[1]))) {
//                BleachLogger.errorMessage("Unable to place wither");
//                setToggled(false);
//                return;
//            }
//
//            boolean rotate = getSetting(0).asToggle().state;
//
//            WorldUtils.placeBlock(pos, block, rotate, false);
//
//            WorldUtils.facePosPacket(
//                    pos.add(-rot[0], 1, -rot[1]).getX() + 0.5, pos.getY() + 1, pos.add(-rot[0], 1, -rot[1]).getZ() + 0.5);
//            WorldUtils.placeBlock(pos.add(0, 1, 0), dispenser, false, false);
//            return;
//
//        }
//
//        BleachLogger.errorMessage("Unable to place 32k");
//        setToggled(false);
//    }
//
//    @Subscribe
//    public void onTick(EventTick event) {
//        if ((getSetting(4).asToggle().state && !active && ticksPassed > 25)) {
//            setToggled(false);
//            return;
//        }
//
//        if (ticksPassed == 1) {
//            //boolean rotate = getSetting(0).toToggle().state;
//
//            WorldUtils.placeBlock(pos, block, false, false);
//            WorldUtils.placeBlock(pos.add(0, 1, 0), dispenser, false, false);
//            mc.player.yaw = startRot[0];
//            mc.player.pitch = startRot[1];
//
//            ticksPassed++;
//            return;
//        }
//
//
//
//    }
//
//}
