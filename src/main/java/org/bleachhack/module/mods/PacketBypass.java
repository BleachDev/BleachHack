package org.bleachhack.module.mods;

import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import org.bleachhack.event.events.EventPacket;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.ModuleSetting;

public class PacketBypass extends Module {
    public PacketBypass() {
        super("PacketBypass", KEY_UNBOUND, ModuleCategory.WORLD, "Bypasses LiveOverflow's packet shenanigans.");
    }

    @BleachSubscribe
    public void readPacket(EventPacket.Read event) {
        if (event.getPacket() instanceof GameStateChangeS2CPacket packet) {
            if (packet.getReason() == GameStateChangeS2CPacket.GAME_MODE_CHANGED || packet.getReason() == GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN) {
                event.setCancelled(true);
            }
        } else if (event.getPacket() instanceof WorldBorderInitializeS2CPacket) {
            event.setCancelled(true);
        }
    }
}