package bleach.hack.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;

public class BleachLogger {

	public static void infoMessage(String s) {
		try{ MinecraftClient.getInstance().inGameHud.getChatHud()
			.addMessage(new LiteralText("§5[BleachHack] §9§lINFO: §9" + s));
		}catch(Exception e) { System.out.println("§5[BleachHack] §9§lINFO: §9" + s); }
	}
	
	public static void warningMessage(String s) {
		try{ MinecraftClient.getInstance().inGameHud.getChatHud()
			.addMessage(new LiteralText("§5[BleachHack] §e§lWARN: §e" + s));
		}catch(Exception e) { System.out.println("§5[BleachHack] §e§lWARN: §e" + s); }
	}
	
	public static void errorMessage(String s) {
		try{ MinecraftClient.getInstance().inGameHud.getChatHud()
			.addMessage(new LiteralText("§5[BleachHack] §c§lERROR: §c" + s));
		}catch(Exception e) { System.out.println("§5[BleachHack] §c§lERROR: §c" + s); }
	}
	
	public static void noPrefixMessage(String s) {
		try{ MinecraftClient.getInstance().inGameHud.getChatHud()
			.addMessage(new LiteralText(s));
		}catch(Exception e) { System.out.println(s); }
	}
}
