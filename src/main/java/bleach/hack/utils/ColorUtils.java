package bleach.hack.utils;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Colours;

public class ColorUtils {
    public static int guiColour() {
        if (ModuleManager.getModule(Colours.class).getSetting(0).asToggle().state) return Rainbow.getInt();
        int red;
        int green;
        int blue;
        red = (int) ModuleManager.getModule(Colours.class).getSetting(1).asSlider().getValue();
        green = (int) ModuleManager.getModule(Colours.class).getSetting(2).asSlider().getValue();
        blue = (int) ModuleManager.getModule(Colours.class).getSetting(3).asSlider().getValue();
        return (0xff << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
    }
    public static int textColor() {
        int red;
        int green;
        int blue;
        red = (int) ModuleManager.getModule(Colours.class).getSetting(4).asSlider().getValue();
        green = (int) ModuleManager.getModule(Colours.class).getSetting(5).asSlider().getValue();
        blue = (int) ModuleManager.getModule(Colours.class).getSetting(6).asSlider().getValue();
        return (0xff << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
    }
}
