package bleach.hack.module.mods;

import bleach.hack.event.events.EventSignBlockEntityRender;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;

public class NoRender extends Module {

	public NoRender() {
		super("NoRender", KEY_UNBOUND, Category.RENDER, "Blocks certain elements from rendering",
				new SettingToggle("Blindness", true), // 0
				new SettingToggle("Fire", true), // 1
				new SettingToggle("Hurtcam", true), // 2
				new SettingToggle("Liquid", true), // 3
				new SettingToggle("Pumpkin", true), // 4
				new SettingToggle("Signs", false), // 5
				new SettingToggle("Wobble", true), // 6
				new SettingToggle("BossBar", false), // 7
				new SettingToggle("Totem-WIP", false), // 8
				new SettingToggle("Shield-WIP", false) // 9
				);
	}

	@Subscribe
	public void signRender(EventSignBlockEntityRender event) {
		if (this.getSettings().get(5).asToggle().state) event.setCancelled(true);
	}
}
