package bleach.hack;

import bleach.hack.module.mods.ClickGui;
import bleach.hack.utils.file.BleachFileReader;
import net.fabricmc.api.ClientModInitializer;

public class BleachHack implements ClientModInitializer {
	
	public static String VERSION = "B6";
	public static int INTVERSION = 9;
	
	@Override
	public void onInitializeClient() {
		BleachFileReader.readModules();
    	BleachFileReader.readSettings();
    	ClickGui.clickGui.initWindows();
    	BleachFileReader.readClickGui();
	}
}
