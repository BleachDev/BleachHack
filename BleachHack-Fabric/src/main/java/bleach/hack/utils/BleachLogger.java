package bleach.hack.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;

public class BleachLogger {

	public static void infoMessage(String s) {
		MinecraftClient.getInstance().inGameHud.getChatHud()
			.addMessage(new LiteralText("§5[BleachHack] §9§lINFO: §9" + s));
	}
	
	public static void warningMessage(String s) {
		MinecraftClient.getInstance().inGameHud.getChatHud()
			.addMessage(new LiteralText("§5[BleachHack] §e§lWARNING: §e" + s));
	}
	
	public static void errorMessage(String s) {
		MinecraftClient.getInstance().inGameHud.getChatHud()
			.addMessage(new LiteralText("§5[BleachHack] §c§lERROR: §c" + s));
	}
	
	public static void noPrefixMessage(String s) {
		MinecraftClient.getInstance().inGameHud.getChatHud()
			.addMessage(new LiteralText(s));
	}
}
