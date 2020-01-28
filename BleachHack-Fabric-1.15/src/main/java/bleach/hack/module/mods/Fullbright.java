package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import com.google.common.eventbus.Subscribe;
import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class Fullbright extends Module {
			
	public Fullbright() {
		super("Fullbright", GLFW.GLFW_KEY_C, Category.RENDER, "Turns your gamma setting up.",
				new SettingMode("Mode: ", "Gamma", "Potion"));
	}

	// table setting 🅱️roke
	
	@Override
	public void onDisable() {
		super.onDisable();
		mc.options.gamma = 1;
		mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
		//Vanilla code to remap light level table.
		/*for(int i = 0; i <= 15; ++i) {
			float float_2 = 1.0F - (float)i / 15.0F;
			mc.world.dimension.getLightLevelToBrightness()[i] = (1.0F - float_2) / (float_2 * 3.0F + 1.0F) * 1.0F + 0.0F;
		}*/
	}

	@Subscribe
	public void onTick(EventTick event) {
		if(getSettings().get(0).toMode().mode == 0) {
			if(mc.options.gamma < 16) mc.options.gamma += 1.2;
		} else if(getSettings().get(0).toMode().mode == 1) {
			mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1, 5));
		}/* else if(getSettings().get(0).toMode().mode == 2) {
			for(int i = 0; i < 16; i++) {
				if(mc.world.dimension.getLightLevelToBrightness()[i] != 1) {
					mc.world.dimension.getLightLevelToBrightness()[i] = 1;
				}
			}
		}*/
	}
}
