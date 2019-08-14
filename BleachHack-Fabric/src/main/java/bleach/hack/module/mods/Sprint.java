package bleach.hack.module.mods;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;

public class Sprint extends Module {
	
	public Sprint() {
		super("Sprint", -1, Category.MOVEMENT, "Makes the player automatically sprint.", null);
	}

	@Override
	public void onEnable() {
		BleachHack.getEventBus().register(this);
	}

	@Override
	public void onDisable() {
		BleachHack.getEventBus().unregister(this);
	}

	@Subscribe
	public void onTick(EventTick eventTick) {
		if(!isToggled()) return;
		mc.player.setSprinting(mc.player.input.movementForward > 0 && mc.player.input.movementSideways != 0 ||
				mc.player.input.movementForward > 0 && !mc.player.isSneaking());
	}
}
