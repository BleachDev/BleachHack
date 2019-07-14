package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class FastUse extends Module {
	
	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"Single", "Multi"}, "Mode: "),
			new SettingSlider(1, 100, 20, 0, "Multi: "));
	
	public FastUse() {
		super("FastUse", GLFW.GLFW_KEY_B, Category.PLAYER, "Allows you to use items faster", settings);
	}
	
	public void onUpdate() {
		if(this.isToggled()) {
			try {
				/* set rightClickDelay to 0 */
				ObfuscationReflectionHelper.findField(Minecraft.class, "field_71467_ac").setInt(mc, 0);
				
				/* call rightClickMouse */
				if(getSettings().get(0).toMode().mode == 1 && mc.gameSettings.keyBindUseItem.isKeyDown()) {
					for(int i = 0; i < (int) getSettings().get(1).toSlider().getValue(); i++) {
						ObfuscationReflectionHelper.findMethod(Minecraft.class, "func_147121_ag").invoke(mc);
					}
				}
			} catch (Exception e) {}
		}
	}

}
