package bleach.hack.module.mods;

import java.awt.Desktop;
import java.net.URI;
import bleach.hack.module.Category;
import bleach.hack.module.Module;

public class StarGithubPls extends Module {

	public StarGithubPls() {
		super("StarGithubPls", -1, Category.MISC, "i need to feed my 420 children pls star github", null);
	}
	
	public void onEnable() {
		try {
			Desktop.getDesktop().browse(new URI("https://github.com/BleachDrinker420/bleachhack-1.14"));
		} catch (Exception e) {e.printStackTrace();}
		
		this.setToggled(false);
	}

}
