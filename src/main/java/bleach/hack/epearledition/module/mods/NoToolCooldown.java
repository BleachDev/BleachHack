package bleach.hack.epearledition.module.mods;

import bleach.hack.epearledition.module.Category;
import bleach.hack.epearledition.module.Module;
import bleach.hack.epearledition.setting.base.SettingSlider;

public class NoToolCooldown extends Module {

    public NoToolCooldown() {
        super("NoToolCooldown", KEY_UNBOUND, Category.COMBAT, "No Tool Cooldown",
                new SettingSlider("Timer", 7, 20, 7, 1));
    }
}
