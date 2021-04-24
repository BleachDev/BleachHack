package bleach.hack.epearledition.module.mods;

import bleach.hack.epearledition.event.events.EventTick;
import bleach.hack.epearledition.module.Category;
import bleach.hack.epearledition.module.Module;
import bleach.hack.epearledition.setting.other.SettingRotate;
import bleach.hack.epearledition.utils.CrystalUtils;
import bleach.hack.epearledition.utils.WorldUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoRespawnAnchor extends Module {
    int ticksPassed;
    boolean enabled;
    BlockPos lookingCoords;
    public AutoRespawnAnchor() {
        super("AutoRespawnAnchor", KEY_UNBOUND, Category.COMBAT, "dumps variables into console",
        new SettingRotate(true));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.player == null) {
            super.setToggled(false);
            return;
        }
        ticksPassed = 0;
        enabled = true;
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {return;}
        lookingCoords = mc.crosshairTarget.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) mc.crosshairTarget).getBlockPos() : null;
    }

    @Subscribe
    public void onTick(EventTick event) {
        assert mc.player != null;
        assert mc.interactionManager != null;
        assert mc.world != null;
        ticksPassed++;
        if (!enabled) return;
        int oldHand = mc.player.inventory.selectedSlot;
        if (ticksPassed == 1) {
            CrystalUtils.changeHotbarSlotToItem(Items.RESPAWN_ANCHOR);
        }
        if (ticksPassed == 2) {
            WorldUtils.newPlaceBlock(lookingCoords.up(), mc.player.inventory.selectedSlot, getSetting(0).asRotate(), true, true);
        }
        if (ticksPassed == 3) {
            CrystalUtils.changeHotbarSlotToItem(Items.GLOWSTONE);
        }
        if (ticksPassed == 4) {
            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(Vec3d.of(lookingCoords), Direction.UP, lookingCoords.up(), true));
        }
        if (ticksPassed == 5) {
            CrystalUtils.changeHotbarSlotToItem(Items.DIAMOND_SWORD);
        }
        if (ticksPassed == 6) {
            mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(Vec3d.of(lookingCoords), Direction.UP, lookingCoords.up(), true));
        }

    }
}
