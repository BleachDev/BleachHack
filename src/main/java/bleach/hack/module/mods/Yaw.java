package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import com.google.common.eventbus.Subscribe;
import net.minecraft.util.math.MathHelper;

public class Yaw extends Module {

    public Yaw() {
        super("Yaw", KEY_UNBOUND, Category.PLAYER, "Locks your cursor to keep you in a cardinal direction.",
                new SettingToggle("Auto", true).withDesc("Auto-centers you to the nearest block"),
                new SettingSlider("Degrees", 1, 360, 180, 0).withDesc("Degrees yaw should lock you to"),
                new SettingSlider("Slice", 1, 8, 8, 0).withDesc("I don't know what this does but its in Kami"));
    }
    @Subscribe
    public void onTick(EventTick event) {
        if (getSetting(2).asSlider().getValue() == 0) return;
        if (getSetting(0).asToggle().state) {
            int angle = 360 / (int) getSetting(2).asSlider().getValue();
            float yaw = mc.player.yaw;
            yaw = Math.round(yaw / angle) * angle;
            mc.player.yaw = yaw;
            if (mc.player.isRiding()) mc.player.getVehicle().yaw = yaw;
        } else {
            float yaw = mc.player.yaw;
            mc.player.yaw = MathHelper.clamp(yaw - 180, -180, 180);
        }
    }
}
