package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.module.setting.base.SettingSlider;

public class Twerk extends Module {
    private int speed;
    public Twerk() {
        super("Twerk", KEY_UNBOUND, ModuleCategory.MISC, "Shake dat booty.",
                new SettingSlider("Speed", 1, 10, 1, 1).withDesc("Twerk speed."));
    }

    @BleachSubscribe
    public void onTick(EventTick event) {
        speed++;
        if (speed < 10 - getSettings().get(0).asSlider().getValueInt())
            return;

        mc.options.keySneak.setPressed(!mc.options.keySneak.isPressed());
        speed = -1;
    }

}
