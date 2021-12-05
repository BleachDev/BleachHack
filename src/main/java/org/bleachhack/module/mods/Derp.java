package org.bleachhack.module.mods;

import java.util.Random;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.module.setting.base.SettingSlider;

public class Derp extends Module {
    public Derp() {
        super("Derp", KEY_UNBOUND, ModuleCategory.FUN, "Makes you do weird stuff.",
                new SettingSlider("Speed", 1, 10, 5, 1).withDesc("Derp speed."));
    }

    private final Random random = new Random();

    @BleachSubscribe
    public void onTick(EventTick event) {

        float yaw = (mc.player.getYaw() + random.nextFloat() * 360F - 180F * getSetting(0).asSlider().getValueFloat() / 10f);
        float pitch = (random.nextFloat() * 180F - 90F * getSetting(0).asSlider().getValueFloat() / 10f);

        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
                yaw, pitch, mc.player.isOnGround()));
    }

    @Override
    public void onDisable(boolean inWorld) {
        super.onDisable(inWorld);
    }
}