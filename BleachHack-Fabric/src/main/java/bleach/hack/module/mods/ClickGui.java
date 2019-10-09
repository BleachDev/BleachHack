package bleach.hack.module.mods;

import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.ClickGuiScreen;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;

public class ClickGui extends Module {
	
	public static ClickGuiScreen clickGui = new ClickGuiScreen();
	
	public ClickGui() {
		super("ClickGui", GLFW.GLFW_KEY_RIGHT_SHIFT, Category.RENDER, "Draws the clickgui",
				new SettingMode("Theme: ", "Light", "Dark", "Future"),
				new SettingSlider(50, 80, 68, 0, "Length: "));
	}
	
	public void onEnable() {
		mc.openScreen(clickGui);
		setToggled(false);
	}

}
