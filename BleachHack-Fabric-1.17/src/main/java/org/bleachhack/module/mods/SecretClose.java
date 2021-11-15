package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventSendPacket;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;

import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public class SecretClose extends Module {

	public SecretClose() {
		super("SecretClose", KEY_UNBOUND, ModuleCategory.EXPLOITS, "Makes the server think you're still in a container after closing it.");
	}

	@BleachSubscribe
	public void onSendPacket(EventSendPacket event) {
		if (event.getPacket() instanceof CloseHandledScreenC2SPacket) {
			event.setCancelled(true);
		}
	}

}
