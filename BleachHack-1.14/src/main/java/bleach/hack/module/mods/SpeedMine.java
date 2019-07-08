package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

public class SpeedMine extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"1", "2", "3"}, "Haste: "));
	
	public SpeedMine() {
		super("SpeedMine", -1, Category.EXPLOITS, "Allows you to mine at sanic speeds", settings);
	}
	
	public void onUpdate() {
		if(this.isToggled()) {
			mc.player.addPotionEffect(new EffectInstance(Effects.HASTE, 1, getSettings().get(0).toMode().mode));
		}
	}
	
	public void onDisable() {
		mc.player.removeActivePotionEffect(Effects.HASTE);
	}

}
