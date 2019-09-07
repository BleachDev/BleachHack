package bleach.hack;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.module.mods.Speed;
import bleach.hack.utils.file.BleachFileReader;
import com.google.common.eventbus.EventBus;
import net.fabricmc.api.ClientModInitializer;

public class BleachHack implements ClientModInitializer {
	
	public static String VERSION = "B8";
	public static int INTVERSION = 12;
	public static EventBus eventBus;

	@Override
	public void onInitializeClient() {
		eventBus = new EventBus();
		BleachFileReader.readModules();
    	BleachFileReader.readSettings();
    	BleachFileReader.readBinds();
    	ClickGui.clickGui.initWindows();
    	BleachFileReader.readClickGui();

    	//v This makes a scat fetishist look like housekeeping.
    	eventBus.register(new ModuleManager());
    	
    	// i have no idea why it enabled speed and resets it everytime you restart but its annoying as fuck
    	ModuleManager.getModule(Speed.class).setToggled(false);
	}
}
