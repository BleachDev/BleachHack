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

public class AutoMLG extends Module {
    public AutoMLG() {
        super("AutoMLG", KEY_UNBOUND, ModuleCategory.PLAYER, "Prevents you from taking fall damage.",
                new SettingMode("Mode", "Cobweb", "Water").withDesc("What mode to use."),
                new SettingRotate(false).withDesc("Rotates when placing blocks (Cobweb mode)"),
                new SettingToggle("LegitPlace", false).withDesc("Only places on sides you can see (Cobweb mode)"),
                new SettingToggle("AirPlace", false).withDesc("Places blocks in the air without support blocks (Cobweb mode)"),
                new SettingToggle("NoSwing", false).withDesc("Doesn't swing your hand clientside (Cobweb mode)"));
    }

    private void main(String item) {
        if (mc.player.fallDistance > 2.5f) {
            int slot = InventoryUtils.getSlot(false, i -> mc.player.getInventory().getStack(i).getItem()
                    == Registry.ITEM.get(new Identifier(item)));
            if (slot == -1) return;
            WorldUtils.placeBlock(
                    new BlockPos(mc.player.getPos().add(0, -1, 0)), slot,
                    getSetting(1).asRotate(),
                    getSetting(2).asToggle().state,
                    getSetting(3).asToggle().state,
                    !getSetting(4).asToggle().state);
        }
    }

    @BleachSubscribe
    public void onTick(EventTick event) {
        switch(getSetting(0).asMode().mode) {
            case 0:
                main("cobweb");
                break;
            case 1:
                main("water_bucket");
                break;
        }
    }
}
