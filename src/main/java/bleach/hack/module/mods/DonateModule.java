package bleach.hack.module.mods;

import java.awt.Desktop;
import java.net.URI;

import bleach.hack.module.Category;
import bleach.hack.module.ModuleManager;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.module.Module;

import static bleach.hack.module.Module.KEY_UNBOUND;

public class DonateModule extends Module
{
    public DonateModule() {
        super("Donate!", KEY_UNBOUND, Category.MISC, "give epearl money");
    }

    @Override
    public void onEnable()
    {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("https://www.enderpearl.live"));
            }
        } catch (Exception e) {e.printStackTrace();}
        ModuleManager.getModule(DonateModule.class).toggle();
    }

}
