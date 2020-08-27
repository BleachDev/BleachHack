package bleach.hack.module.mods;

import bleach.hack.event.Event;
import bleach.hack.event.events.EventTick;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;

public class NoToolCooldown extends Module {

    public NoToolCooldown() {
        super("NoToolCooldown", KEY_UNBOUND, Category.COMBAT, "No Tool Cooldown",
                new SettingSlider("Timer", 7, 20, 7, 1));
    }
}
