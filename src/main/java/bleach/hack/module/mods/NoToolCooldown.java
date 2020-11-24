package bleach.hack.module.mods;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;

public class NoToolCooldown extends Module {

    public NoToolCooldown() {
        super("NoToolCooldown", KEY_UNBOUND, Category.COMBAT, "No Tool Cooldown",
                new SettingSlider("Timer", 7, 20, 7, 1));
    }
}
