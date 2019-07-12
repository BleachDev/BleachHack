package bleach.hack.module.mods;

import bleach.hack.module.Category;
import bleach.hack.module.Module;

public class Sprint extends Module {
	
	public Sprint() {
		super("Sprint", -1, Category.MOVEMENT, "Makes the player automatically sprint.", null);
	}
	
	public void onUpdate() {
		if(this.isToggled()) {
			if(mc.player.moveStrafing != 0 || mc.player.moveForward != 0) {
				mc.player.setSprinting(true);
			}
		}
	}

}
