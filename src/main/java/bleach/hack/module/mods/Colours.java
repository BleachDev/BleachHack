package bleach.hack.module.mods;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import org.lwjgl.glfw.GLFW;

public class Colours extends Module {
    public Colours() {
        super("ColourChooser", GLFW.GLFW_KEY_RIGHT_SHIFT, Category.CLIENT, "HUD color settings",
                new SettingToggle("Rainbow", false),
                new SettingSlider("Red", 0, 255, 85, 1),
                new SettingSlider("Green", 0, 255, 85, 1),
                new SettingSlider("Blue", 0, 255, 255, 1),
                new SettingSlider("TextRed", 0, 255, 255, 1),
                new SettingSlider("TextGreen", 0, 255, 255, 1),
                new SettingSlider("TextBlue", 0, 255, 255, 1));
    }

    @Override
    public void onEnable() {
        setToggled(false);
    }
}