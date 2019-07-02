package bleach.hack.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;

public class BleachLogger {

	public static void infoMessage(String s) {
		Minecraft.getInstance().ingameGUI.getChatGUI()
			.printChatMessage(new StringTextComponent("§5[BleachHack] §9§lINFO: §9" + s));
	}
	
	public static void warningMessage(String s) {
		Minecraft.getInstance().ingameGUI.getChatGUI()
			.printChatMessage(new StringTextComponent("§5[BleachHack] §e§lWARNING: §e" + s));
	}
	
	public static void errorMessage(String s) {
		Minecraft.getInstance().ingameGUI.getChatGUI()
			.printChatMessage(new StringTextComponent("§5[BleachHack] §c§lERROR: §c" + s));
	}
}
