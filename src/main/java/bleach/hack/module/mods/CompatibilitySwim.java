package bleach.hack.module.mods;

import bleach.hack.event.events.EventClientMove;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.utils.BleachLogger;
import com.google.common.eventbus.Subscribe;

public class CompatibilitySwim extends Module {

    public CompatibilitySwim() {
        super("1.12Swim", KEY_UNBOUND, Category.MISC, "Swims slower in 1.12 protocol to not trigger anticheat",
                new SettingSlider("Move Speed", 0.1, 10, 2, 2));
    }

    @Subscribe
    public void onTick(EventTick event) {
        double speedstrafe = getSetting(0).asSlider().getValue() / 3;
        double forward = mc.player.forwardSpeed;
        double strafe = mc.player.sidewaysSpeed;
        float yaw = mc.player.yaw;

        if (mc.player.isSwimming() && !ModuleManager.getModule(Speed.class).isToggled()) {
            if ((forward == 0.0D) && (strafe == 0.0D)) {
                mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
            } else {
                if (forward != 0.0D) {
                    if (strafe > 0.0D) {
                        yaw += (forward > 0.0D ? -45 : 45);
                    } else if (strafe < 0.0D) yaw += (forward > 0.0D ? 45 : -45);
                    strafe = 0.0D;
                    if (forward > 0.0D) {
                        forward = 1.0D;
                    } else if (forward < 0.0D) forward = -1.0D;
                }
                mc.player.setVelocity((forward * speedstrafe * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speedstrafe * Math.sin(Math.toRadians(yaw + 90.0F))), mc.player.getVelocity().y,
                        forward * speedstrafe * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speedstrafe * Math.cos(Math.toRadians(yaw + 90.0F)));
            }
        }
    }

}
