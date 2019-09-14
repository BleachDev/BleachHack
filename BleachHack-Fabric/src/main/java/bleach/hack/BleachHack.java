package bleach.hack;

import com.google.common.eventbus.EventBus;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.module.mods.Speed;
import bleach.hack.utils.file.BleachFileHelper;
import bleach.hack.utils.file.BleachFileMang;
import net.fabricmc.api.ClientModInitializer;

public class BleachHack implements ClientModInitializer {
	
	public static String VERSION = "B9";
	public static int INTVERSION = 13;
	public static EventBus eventBus;

	@Override
	public void onInitializeClient() {
		eventBus = new EventBus();
		
		BleachFileMang.init();
		BleachFileHelper.readModules();
    	BleachFileHelper.readSettings();
    	BleachFileHelper.readBinds();
    	
    	ClickGui.clickGui.initWindows();
    	BleachFileHelper.readClickGui();
    	BleachFileHelper.readPrefix();

    	//v This makes a scat fetishist look like housekeeping.
    	eventBus.register(new ModuleManager());
    	// wait why do we need this ^?
    	
    	// i have no idea why it enabled speed and resets it everytime you restart but its annoying as fuck
    	ModuleManager.getModule(Speed.class).setToggled(false);
	}
}
