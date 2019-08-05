package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class SpeedMine extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"1", "2", "3"}, "Haste: "));
	
	public SpeedMine() {
		super("SpeedMine", -1, Category.EXPLOITS, "Allows you to mine at sanic speeds", settings);
	}
	
	public void onUpdate() {
		if(!isToggled()) return;
		
		mc.player.addPotionEffect(new StatusEffectInstance(StatusEffects.HASTE, 1, getSettings().get(0).toMode().mode));
	}
	
	public void onDisable() {
		mc.player.removePotionEffect(StatusEffects.HASTE);
	}

}
