package bleach.hack.epearledition.module.mods;

import bleach.hack.epearledition.module.Category;
import bleach.hack.epearledition.module.Module;
import bleach.hack.epearledition.setting.base.SettingSlider;

public class Timer extends Module {

    public Timer() {
        super("Timer", KEY_UNBOUND, Category.WORLD, "more speeds",
                new SettingSlider("Speed", 0.01, 20, 1, 2));
    }

    // See MixinRenderTickCounter for code

}
