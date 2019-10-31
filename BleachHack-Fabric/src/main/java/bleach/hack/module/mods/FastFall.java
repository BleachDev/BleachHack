package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.WorldUtils;

public class FastFall extends Module {

    public FastFall() {
        super("FastFall", -1, Category.MOVEMENT, "Fall Fast.",
                new SettingSlider("Speed: ",-1, -10, -3, 2));
    }
    public void onTick(EventTick event) {
        if (mc.player.getVelocity().getY() < -0.2 && !WorldUtils.NONSOLID_BLOCKS.contains(mc.world.getBlockState(mc.player.getBlockPos().add(0, -1, 0)).getBlock())) {
            mc.player.setVelocity(mc.player.getVelocity().getX(), -3, mc.player.getVelocity().getZ());
        }
    }
}