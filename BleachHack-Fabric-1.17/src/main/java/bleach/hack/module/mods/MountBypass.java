package bleach.hack.module.mods;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.util.PlayerInteractEntityC2SUtils;
import bleach.hack.util.PlayerInteractEntityC2SUtils.InteractType;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class MountBypass extends Module {

	public boolean dontCancel = false;

	public MountBypass() {
		super("MountBypass", KEY_UNBOUND, Category.EXPLOITS, "Bypasses illegalstack on non bungeecord servers");
	}

	@Subscribe
	public void onPacket(EventSendPacket event) {
		if (dontCancel)
			return;

		if (event.getPacket() instanceof PlayerInteractEntityC2SPacket
				&& PlayerInteractEntityC2SUtils.getEntity(
						(PlayerInteractEntityC2SPacket) event.getPacket()) instanceof AbstractDonkeyEntity
				&& PlayerInteractEntityC2SUtils.getInteractType(
						(PlayerInteractEntityC2SPacket) event.getPacket()) == InteractType.INTERACT_AT) {
			event.setCancelled(true);
		}
	}
}
