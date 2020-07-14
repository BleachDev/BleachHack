package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.mixin.FirstPersonRendererAccessor;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;

public class HandProgress extends Module {

    public HandProgress() {
        super("HandProgress-WIP", KEY_UNBOUND, Category.RENDER, "Smaller view of mainhand/offhand",
                new SettingSlider("Mainhand", 0.1, 1.0, 1.0, 1), // 0
                new SettingSlider("Offhand", 0.1, 1.0, 1.0, 1) // 1
        );
    }

    @Subscribe
    public void tick(EventTick event){
        // this kinda works, but changing item doesn't update..
        FirstPersonRendererAccessor accessor = (FirstPersonRendererAccessor) mc.gameRenderer.firstPersonRenderer;
        accessor.setEquippedProgressMainHand((float) this.getSettings().get(0).toSlider().getValue());
        accessor.setEquippedProgressOffHand((float) this.getSettings().get(1).toSlider().getValue());
    }
}
