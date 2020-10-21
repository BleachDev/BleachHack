package bleach.hack.module.mods;

import java.awt.Desktop;
import java.net.URI;

import bleach.hack.module.Category;
import bleach.hack.module.ModuleManager;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.module.Module;

import static bleach.hack.module.Module.KEY_UNBOUND;

public class DiscordsModule extends Module
{
    public DiscordsModule() {
        super("Join Discords", KEY_UNBOUND, Category.MISC, "join discord");
    }

    @Override
    public void onEnable()
    {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("https://www.discord.gg/VXJ7eY9"));
            }
        } catch (Exception e) {e.printStackTrace();}
        ModuleManager.getModule(DiscordsModule.class).toggle();
    }

}