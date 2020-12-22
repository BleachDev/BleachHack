package bleach.hack.module.mods;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.CrystalUtils;
import bleach.hack.utils.WorldUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoSelfWeb extends Module {

    BlockPos playerPos;
    boolean rotate = getSetting(0).asToggle().state;
    int lastSlot;

    public AutoSelfWeb() {
        super("AutoSelfWeb", KEY_UNBOUND, Category.COMBAT, "Automatically self webs",
                new SettingToggle("Rotate", true));
    }

    @Subscribe
    public void onSendPacket(EventTick event) {
        playerPos = mc.player.getBlockPos();
        lastSlot = mc.player.inventory.selectedSlot;

        if (!mc.world.getOtherEntities(null, new Box(playerPos)).isEmpty()
                && ((long) mc.world.getOtherEntities(null, new Box(playerPos.add(0,1,0))).size() > 1)
                && !(this.mc.world.getBlockState(playerPos.north()).getBlock() == Blocks.AIR)
                && !(this.mc.world.getBlockState(playerPos.south()).getBlock() == Blocks.AIR)
                && !(this.mc.world.getBlockState(playerPos.east()).getBlock() == Blocks.AIR)
                && !(this.mc.world.getBlockState(playerPos.west()).getBlock() == Blocks.AIR)
                && !(this.mc.world.getBlockState(playerPos).getBlock() == Blocks.COBWEB)
        ) {
            for (int i = 0; i < 9; i++) {
                if (mc.player.inventory.getStack(i).getItem() == Items.COBWEB) {
                        mc.player.inventory.selectedSlot = i;

                        CrystalUtils.placeBlock(new Vec3d(playerPos.getX(), playerPos.getY(), playerPos.getZ()), Hand.MAIN_HAND, Direction.UP);
                        if (lastSlot != -1) {
                            mc.player.inventory.selectedSlot = lastSlot;
                            lastSlot = -1;
                        }
                }
            }
        }
    }

}
