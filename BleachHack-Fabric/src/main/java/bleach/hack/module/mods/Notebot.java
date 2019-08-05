package bleach.hack.module.mods;

import bleach.hack.module.Category;
import bleach.hack.module.Module;

public class Notebot extends Module {

	public Notebot() {
		super("Notebot", -1, Category.MISC, "Plays those noteblocks nicely", null);
	}
	
	public void onUpdate() {
		if(!isToggled()) return;
	}

}
