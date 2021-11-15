package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventSendPacket;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.module.setting.base.SettingMode;

import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;

public class Sneak extends Module {

	private boolean packetSent;

	public Sneak() {
		super("Sneak", KEY_UNBOUND, ModuleCategory.MOVEMENT, "Makes you automatically sneak.",
				new SettingMode("Mode", "Legit", "Packet").withDesc("Mode for sneaking (Only other players will see u sneaking with packet mode)."));
	}

	@Override
	public void onDisable(boolean inWorld) {
		packetSent = false;
		mc.options.keySneak.setPressed(false);

		if (inWorld)
			mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
	
		super.onDisable(inWorld);
	}

	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);

		if (getSetting(0).asMode().mode == 1) {
			if (inWorld)
				mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));

			packetSent = true;
		}
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (getSetting(0).asMode().mode == 0) {
			mc.options.keySneak.setPressed(true);
		} else if (getSetting(0).asMode().mode == 1 && !packetSent) {
			mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
			packetSent = true;
		}
	}

	@BleachSubscribe
	public void onSendPacket(EventSendPacket event) {
		if (event.getPacket() instanceof ClientCommandC2SPacket) {
			ClientCommandC2SPacket p = (ClientCommandC2SPacket) event.getPacket();
			if (p.getMode() == ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY)
				event.setCancelled(true);
		}
	}
}
