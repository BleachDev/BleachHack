package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingBase;
import bleach.hack.utils.CrystalUtils;
import bleach.hack.utils.RenderUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Baconator extends Module {

    float ticks = 0;
    float flint = 0;

    public Baconator() {
        super("Baconator",
                KEY_UNBOUND,
                Category.MISC,
                "Ignites every alive piece of food. P.S. Laggy af");
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Subscribe
    public void onRender(EventWorldRender e) {
        if (!isToggled() || mc.player == null || !mc.player.inventory.getMainHandStack().isItemEqual(Items.FLINT_AND_STEEL.getDefaultStack()))
            return;

        ticks += 1; flint += 1;
        for (Entity ent : mc.world.getEntities()) {
            if (    !(ent instanceof ChickenEntity) &&
                    !(ent instanceof SheepEntity) &&
                    !(ent instanceof CowEntity) &&
                    !(ent instanceof PigEntity) &&
                    !(ent instanceof RabbitEntity))
                continue;

            AnimalEntity animal = (AnimalEntity)ent;
            if (mc.player.getPos().distanceTo(ent.getPos()) > 5)
                continue;

            Block block = mc.world.getBlockState(ent.getBlockPos()).getBlock();
            Block bottom = mc.world.getBlockState(ent.getBlockPos().down()).getBlock();
            if (block.is(Blocks.WATER) || bottom.is(Blocks.GRASS_PATH))
                continue;

            if (block.is(Blocks.GRASS))
                mc.interactionManager.attackBlock(ent.getBlockPos(), Direction.DOWN);

            if (animal.getHealth() < 1) {
                mc.interactionManager.attackBlock(ent.getBlockPos(), Direction.DOWN);
                mc.interactionManager.attackBlock(ent.getBlockPos().west(), Direction.DOWN);
                mc.interactionManager.attackBlock(ent.getBlockPos().east(), Direction.DOWN);
                mc.interactionManager.attackBlock(ent.getBlockPos().north(), Direction.DOWN);
                mc.interactionManager.attackBlock(ent.getBlockPos().south(), Direction.DOWN);
            } else {
                if (ticks >= 40) {
                    mc.player.playSound(SoundEvents.ENTITY_PIGLIN_HURT, 2f, 1);
                    ticks = 0;
                }
                if (flint >= 40) {
                    mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(
                            ent.getPos().subtract(new Vec3d(0, 1, 0)), Direction.UP, ent.getBlockPos().down(), false
                    ));
                    flint = 0;
                }
                RenderUtils.drawFilledBox(ent.getBoundingBox(), 1, 0, 0, .8f);
            }
        }
    }
}
