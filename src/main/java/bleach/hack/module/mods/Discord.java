package bleach.hack.module.mods;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;

import java.awt.*;
import java.net.URI;

public class Discord extends Module
{
    public Discord() {
        super("Discord", KEY_UNBOUND, Category.MISC, "join discord");
    }

    @Override
    public void onEnable()
    {
        try {
            System.out.println("headless toggle property: " + System.getProperty("java.awt.headless"));
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("https://discord.gg/WkdpPZ6"));
            }
        } catch (Exception e) {e.printStackTrace();}
        ModuleManager.getModule(Discord.class).toggle();
    }


}
