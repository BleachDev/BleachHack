package bleach.hack.module.mods;


import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.MinecraftClient;

public class MaintainAltitude extends Module {

    private boolean maintaining = false;

    public MaintainAltitude() {
        super("MaintainAltitude", KEY_UNBOUND, Category.MOVEMENT, "Maintains an altitude above the selected Y (Use with ElytraFly)", new SettingSlider("Y: ", 100, 200, 150, 1));
    }

    public void onDisable() {
        if (maintaining) { // above selected Y
            maintaining = false;
            MinecraftClient.getInstance().options.keyJump.setPressed(false);
        }
        super.onDisable();
    }

    @Subscribe
    public void onTick(EventTick event) {
        if (MinecraftClient.getInstance().player.getPos().y < getSettings().get(0).toSlider().getValue()) {
            maintaining = true;
            MinecraftClient.getInstance().options.keyJump.setPressed(true);
        } else if (maintaining) { // above selected Y
            maintaining = false;
            MinecraftClient.getInstance().options.keyJump.setPressed(false);
        }
    }
}