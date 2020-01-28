package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;

public class Sprint extends Module {
	
	public Sprint() {
		super("Sprint", -1, Category.MOVEMENT, "Makes the player automatically sprint.");
	}

	@Subscribe
	public void onTick(EventTick event) {
		if(!isToggled()) return;
		mc.player.setSprinting(mc.player.input.movementForward > 0 && mc.player.input.movementSideways != 0 ||
				mc.player.input.movementForward > 0 && !mc.player.isSneaking());
	}
}
