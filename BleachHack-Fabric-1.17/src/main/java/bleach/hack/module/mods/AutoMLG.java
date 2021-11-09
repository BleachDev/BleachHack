package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.eventbus.BleachSubscribe;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingBase;
import bleach.hack.module.setting.base.SettingMode;
import bleach.hack.module.setting.base.SettingSlider;
import bleach.hack.module.setting.base.SettingToggle;
import bleach.hack.module.setting.other.SettingRotate;
import bleach.hack.util.InventoryUtils;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AutoMLG extends Module {
    private int maxTimeout = 10;
    private int timeout = 0;

    public AutoMLG() {
        super("AutoMLG", KEY_UNBOUND, ModuleCategory.PLAYER, "Prevents you from taking fall damage in a legit way.",
                new SettingMode("Mode", "Cobweb", "Water").withDesc("What mode to use."),
                new SettingRotate(true).withDesc("Rotates when placing blocks (Always enabled and uses Client for Water Mode)"),
                new SettingToggle("LegitPlace", true).withDesc("Only places on sides you can see"),
                new SettingToggle("AirPlace", false).withDesc("Places blocks in the air without support blocks"),
                new SettingToggle("NoSwing", false).withDesc("Doesn't swing your hand clientside"),
                new SettingToggle("AutoSwitch", true).withDesc("Automatically switch mode for item you have"),
                new SettingSlider("Fall Distance", 2.5f, 6f, 4.5f, 1).withDesc("What minimum distance is required for MLG"));
    }

    @BleachSubscribe
    public void onTick(EventTick event) {
        if (getSetting(0).asMode().mode == 1) {
            getSetting(1).asRotate().state = true;
            getSetting(1).asRotate().setMode(1);
        }
        int cobwebSlot = InventoryUtils.getSlot(false, i -> mc.player.getInventory().getStack(i).getItem()
                == Registry.ITEM.get(new Identifier("cobweb")));
        int bucketSlot = InventoryUtils.getSlot(false, i -> mc.player.getInventory().getStack(i).getItem()
                == Registry.ITEM.get(new Identifier("water_bucket")));
        if (getSetting(5).asToggle().state) {
            if (cobwebSlot == -1 && bucketSlot != -1) getSetting(0).asMode().mode = 1;
            else if (cobwebSlot != -1 && bucketSlot == -1) getSetting(0).asMode().mode = 0;
        }
        if (mc.player.fallDistance < getSetting(6).asSlider().getValueFloat()) return;
        var pos = new BlockPos(mc.player.getPos().add(0, -1, 0));
        int emptyBucketSlot = InventoryUtils.getSlot(false, i -> mc.player.getInventory().getStack(i).getItem()
                == Registry.ITEM.get(new Identifier("bucket")));
        switch (getSetting(0).asMode().mode) {
            case 0:
                if (cobwebSlot == -1) return;
                WorldUtils.placeBlock(
                    pos, cobwebSlot,
                    getSetting(1).asRotate(),
                    getSetting(2).asToggle().state,
                    getSetting(3).asToggle().state,
                    !getSetting(4).asToggle().state);
                break;
            case 1:
                timeout++;
                if (timeout >= maxTimeout) timeout = 0;
                if (timeout == 0 && emptyBucketSlot != -1) WorldUtils.interactItem(pos, emptyBucketSlot,
                        getSetting(1).asRotate(),
                        getSetting(2).asToggle().state,
                        getSetting(3).asToggle().state,
                        !getSetting(4).asToggle().state);
                if (bucketSlot == -1) return;
                WorldUtils.interactItem(pos, bucketSlot,
                        getSetting(1).asRotate(),
                        getSetting(2).asToggle().state,
                        getSetting(3).asToggle().state,
                        !getSetting(4).asToggle().state);
                break;
        }
    }
}
