package bleach.hack;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.utils.file.BleachFileReader;
import com.google.common.eventbus.EventBus;
import net.fabricmc.api.ClientModInitializer;

public class BleachHack implements ClientModInitializer {
	
	public static String VERSION = "B6";
	public static int INTVERSION = 9;
	private static EventBus eventBus;

	@Override
	public void onInitializeClient() {
		eventBus = new EventBus();
		BleachFileReader.readModules();
    	BleachFileReader.readSettings();
    	ClickGui.clickGui.initWindows();
    	BleachFileReader.readClickGui();

    	//v This makes a scat fetishist look like housekeeping.
    	eventBus.register(new ModuleManager());
	}

	//TODO: Move this? I wish this was a bit more OO because I'm so used to accessing everything from one class.
	public static EventBus getEventBus() {
		return eventBus;
	}
}
