package bleach.hack.module.mods;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.CrystalUtils;
import bleach.hack.utils.EntityUtils;
import bleach.hack.utils.WorldUtils;
import com.google.common.collect.Streams;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoBedBomb extends Module {
    int ticksPassed;
    Entity targetPlayer;
    BlockPos targetPlayerPos;
    int bed;
    int currentSlot;

    public AutoBedBomb() {
        super("AutoBedBomb", KEY_UNBOUND, Category.COMBAT, "Automatically places beds on targets",
                new SettingSlider("Delay", 0, 20, 10, 0),
                new SettingSlider("Range", 0, 10, 5, 2)
                );
    }

    @Override
    public void onEnable() {
        super.onEnable();
        assert mc.player != null;
        ticksPassed = 0;
        if (mc.player == null) {
            super.setToggled(false);
            return;
        }
    }


    @Subscribe
    public void onTick(EventTick event) {
        if (!this.isToggled()) {return;}
        ticksPassed++;
        if(ticksPassed < getSetting(0).asSlider().getValue())
            return;
        ticksPassed = 0;


        targetPlayer = Streams.stream(mc.world.getEntities())
                .filter(e -> e instanceof PlayerEntity)
                .filter(e -> !(BleachHack.friendMang.has(e.getName().asString())) && e != mc.player)
                .filter(e -> mc.player.distanceTo(e) < getSetting(1).asSlider().getValue())
                .filter(e -> !((PlayerEntity) e).isDead())
                .sorted((a, b) -> Float.compare(a.distanceTo(mc.player), b.distanceTo(mc.player)))
                .findFirst()
                .orElse(null);
        if(targetPlayer == null)
            return;
        targetPlayerPos = targetPlayer.getBlockPos().up();


        bed = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getStack(i).getItem() instanceof BedItem) {
                bed = i;
                break;
            }
        }

        if (bed == -1) {
            BleachLogger.infoMessage("Out of beds!");
            this.setToggled(false);
            return;
        }

        //BleachLogger.infoMessage("Target: "+targetPlayer.getDisplayName().toString()+" pos: "+targetPlayerPos.toString());
        
        currentSlot = mc.player.inventory.selectedSlot;

        if (this.mc.world.getBlockState(targetPlayerPos.north()).getBlock() == Blocks.AIR || this.mc.world.getBlockState(targetPlayerPos.north()).getBlock() == Blocks.FIRE) {
            mc.player.inventory.selectedSlot = bed;

            WorldUtils.facePosPacket(mc.player.getX(), mc.player.getY(), mc.player.getZ()+1);
            CrystalUtils.placeBlock(Vec3d.of(targetPlayerPos.down().north()), Hand.MAIN_HAND, Direction.UP);
            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
                    new BlockHitResult(mc.player.getPos(), Direction.UP, targetPlayerPos.north(), true));

            mc.player.inventory.selectedSlot = currentSlot;
        } else if (this.mc.world.getBlockState(targetPlayerPos.south()).getBlock() == Blocks.AIR || this.mc.world.getBlockState(targetPlayerPos.south()).getBlock() == Blocks.FIRE) {
            mc.player.inventory.selectedSlot = bed;

            WorldUtils.facePosPacket(mc.player.getX(), mc.player.getY(), mc.player.getZ()-1);
            CrystalUtils.placeBlock(Vec3d.of(targetPlayerPos.down().south()), Hand.MAIN_HAND, Direction.UP);
            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
                    new BlockHitResult(mc.player.getPos(), Direction.UP, targetPlayerPos.south(), true));

            mc.player.inventory.selectedSlot = currentSlot;
        } else if (this.mc.world.getBlockState(targetPlayerPos.east()).getBlock() == Blocks.AIR || this.mc.world.getBlockState(targetPlayerPos.east()).getBlock() == Blocks.FIRE) {
            mc.player.inventory.selectedSlot = bed;

            WorldUtils.facePosPacket(mc.player.getX()-1, mc.player.getY(), mc.player.getZ());
            CrystalUtils.placeBlock(Vec3d.of(targetPlayerPos.down().east()), Hand.MAIN_HAND, Direction.UP);
            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
                    new BlockHitResult(mc.player.getPos(), Direction.UP, targetPlayerPos.east(), true));

            mc.player.inventory.selectedSlot = currentSlot;
        } else if (this.mc.world.getBlockState(targetPlayerPos.west()).getBlock() == Blocks.AIR || this.mc.world.getBlockState(targetPlayerPos.west()).getBlock() == Blocks.FIRE) {
            mc.player.inventory.selectedSlot = bed;

            WorldUtils.facePosPacket(mc.player.getX()+1, mc.player.getY(), mc.player.getZ());
            CrystalUtils.placeBlock(Vec3d.of(targetPlayerPos.down().west()), Hand.MAIN_HAND, Direction.UP);
            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
                    new BlockHitResult(mc.player.getPos(), Direction.UP, targetPlayerPos.west(), true));

            mc.player.inventory.selectedSlot = currentSlot;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        targetPlayer = null;
        ticksPassed = 0;
    }
}
