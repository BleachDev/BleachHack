package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.ClickGuiScreen;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;

public class ClickGui extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingSlider(50, 80, 64, 0, "Length: "),
			new SettingSlider(50, 90, 70, 0, "Length2: "));
	
	public static ClickGuiScreen clickGui = new ClickGuiScreen();
	
	public ClickGui() {
		super("ClickGui", GLFW.GLFW_KEY_RIGHT_SHIFT, Category.RENDER, "Draws the clickgui", settings);
	}
	
	public void onEnable() {
		mc.openScreen(clickGui);
	}

}
