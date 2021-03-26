package bleach.hack.module.mods;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventOpenScreen;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.BleachQueue;
import net.minecraft.client.gui.screen.DeathScreen;

public class AutoRespawn extends Module {

	public AutoRespawn() {
		super("AutoRespawn", KEY_UNBOUND, Category.PLAYER, "Automatically respawn when you die",
				new SettingToggle("Delay", false).withDesc("Adds a delay before respawing").withChildren(
						new SettingSlider("Delay", 1, 15, 5, 0).withDesc("How many ticks to delay")));
	}

	@Subscribe
	public void onOpenScreen(EventOpenScreen event) {
		if (event.getScreen() instanceof DeathScreen) {
			if (getSetting(0).asToggle().state) {
				for (int i = 0; i <= (int) getSetting(0).asToggle().getChild(0).asSlider().getValue(); i++)
					BleachQueue.add("autorespawn", () -> {
					});
				BleachQueue.add("autorespawn", () -> mc.player.requestRespawn());
			} else {
				mc.player.requestRespawn();
			}
		}
	}
}
