package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.eventbus.BleachSubscribe;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingBase;
import bleach.hack.module.setting.base.SettingMode;
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
    public AutoMLG() {
        super("AutoMLG", KEY_UNBOUND, ModuleCategory.PLAYER, "Prevents you from taking fall damage in a legit way.",
                new SettingMode("Mode", "Cobweb", "Water").withDesc("What mode to use."),
                new SettingRotate(false).withDesc("Rotates when placing blocks (Warning: Should be enabled for Water Mode)"),
                new SettingToggle("LegitPlace", false).withDesc("Only places on sides you can see"),
                new SettingToggle("AirPlace", false).withDesc("Places blocks in the air without support blocks"),
                new SettingToggle("NoSwing", false).withDesc("Doesn't swing your hand clientside"));
    }

    @BleachSubscribe
    public void onTick(EventTick event) {
        var pos = new BlockPos(mc.player.getPos().add(0, -1, 0));
        if (mc.player.fallDistance < 2.5f) return;
        int slot;
        switch(getSetting(0).asMode().mode) {
            case 0:
                slot = InventoryUtils.getSlot(false, i -> mc.player.getInventory().getStack(i).getItem()
                    == Registry.ITEM.get(new Identifier("cobweb")));
                if (slot == -1) return;
                WorldUtils.placeBlock(
                    pos, slot,
                    getSetting(1).asRotate(),
                    getSetting(2).asToggle().state,
                    getSetting(3).asToggle().state,
                    !getSetting(4).asToggle().state);
                break;
            case 1:
                slot = InventoryUtils.getSlot(false, i -> mc.player.getInventory().getStack(i).getItem()
                    == Registry.ITEM.get(new Identifier("water_bucket")));
                if (slot == -1) return;
                var hand = InventoryUtils.selectSlot(slot);
                WorldUtils.interactItem(pos, slot,
                        getSetting(1).asRotate(),
                        getSetting(2).asToggle().state,
                        getSetting(3).asToggle().state,
                        !getSetting(4).asToggle().state);
                break;
        }
    }
}
