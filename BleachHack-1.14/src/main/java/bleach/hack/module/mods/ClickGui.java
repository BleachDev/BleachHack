package bleach.hack.module.mods;

import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.ClickGuiScreen;
import bleach.hack.module.Category;
import bleach.hack.module.Module;

public class ClickGui extends Module{

	public ClickGui() {
		super("ClickGui", GLFW.GLFW_KEY_RIGHT_SHIFT, Category.RENDER, "Draws the clickgui", null);
	}
	
	public void onEnable() {
		mc.displayGuiScreen(new ClickGuiScreen(null));
	}

}
