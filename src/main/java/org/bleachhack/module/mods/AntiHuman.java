package org.bleachhack.module.mods;

import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import org.bleachhack.event.events.EventPacket;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.ModuleSetting;
import org.bleachhack.setting.module.SettingToggle;

public class AntiHuman extends Module {
    public AntiHuman() {
        super(
                "AntiHuman", KEY_UNBOUND, ModuleCategory.MOVEMENT, "Bypasses LiveOverflow's anti-human plugin.",
                new SettingToggle("Strict", false)
        );
    }

    @BleachSubscribe
    public void sendPacket(EventPacket.Send event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket oldPacket) {
            if (!oldPacket.changesPosition() && !getSetting(0).asToggle().getState()) return;
            PlayerMoveC2SPacket packet = new PlayerMoveC2SPacket.Full(
                    Math.round(oldPacket.getX(0.0)*10.)/10.,
                    oldPacket.getY(0.0),
                    Math.round(oldPacket.getZ(0.0)*10.)/10.,
                    oldPacket.getYaw(0.0F),
                    oldPacket.getPitch(0.0F),
                    oldPacket.isOnGround()
            );
            mc.player.networkHandler.sendPacket(packet);
            event.setCancelled(true);
        } else if (event.getPacket() instanceof VehicleMoveC2SPacket oldPacket) {
            VehicleMoveC2SPacket packet = new VehicleMoveC2SPacket(new BoatEntity(
                    mc.world,
                    Math.round(oldPacket.getX()*10.)/10.,
                    oldPacket.getY(),
                    Math.round(oldPacket.getZ()*10.)/10.
            ));
            mc.player.networkHandler.sendPacket(packet);
            event.setCancelled(true);
        }
    }
}