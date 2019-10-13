package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class SpeedMine extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode("Mode: ", "Haste", "OG"),
			new SettingSlider(1, 3, 1, 0, "Haste Level: "),
			new SettingSlider(0, 4, 1, 0, "Cooldown: "),
			new SettingSlider(1, 3, 1.3, 1, "Multiplier: "));

	public SpeedMine() {
		super("SpeedMine", -1, Category.EXPLOITS, "Allows you to mine at sanic speeds", settings);
	}

	@Override
	public void onDisable() {
		super.onDisable();
		mc.player.removePotionEffect(StatusEffects.HASTE);
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (this.getSettings().get(0).toMode().mode == 0) {
			mc.player.addPotionEffect(new StatusEffectInstance(StatusEffects.HASTE, 1, (int) getSettings().get(1).toSlider().getValue()));
		}
	}
}
