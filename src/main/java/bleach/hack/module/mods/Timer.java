package bleach.hack.module.mods;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;

public class Timer extends Module {

    public Timer() {
        super("Timer", KEY_UNBOUND, Category.WORLD, "more speeds",
                new SettingSlider("Speed", 0.01, 20, 1, 2));
    }

    // See MixinRenderTickCounter for code

}
