package bleach.hack.module.mods;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.util.SystemUtil;

public class StarGithub extends Module {

	public StarGithub() {
		super("StarGithub", -1, Category.MISC, "i need to feed my 420 children pls star github");
	}
	
	public void onEnable() {
		try {
			SystemUtil.getOperatingSystem().open("https://github.com/BleachDrinker420/bleachhack-1.14");
		} catch (Exception e) {e.printStackTrace();}
		
		this.setToggled(false);
	}
}
