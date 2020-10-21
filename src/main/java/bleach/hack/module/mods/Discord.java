package bleach.hack.module.mods;

import java.awt.Desktop;
import java.net.URI;

import bleach.hack.module.Category;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.Module;

public class Discord extends Module
{
    public Discord() {
        super("Discord", KEY_UNBOUND, Category.MISC, "join discord");
    }

    @Override
    public void onEnable()
    {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("https://discord.gg/WkdpPZ6"));
            }
        } catch (Exception e) {e.printStackTrace();}
        ModuleManager.getModule(Discord.class).toggle();
    }

}
