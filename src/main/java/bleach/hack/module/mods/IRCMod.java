package bleach.hack.module.mods;

import baritone.api.event.events.WorldEvent;
import bleach.hack.command.Command;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.IRCManager;
import bleach.hack.utils.file.BleachFileMang;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IRCMod extends Module {

    public IRCMod() {
        super("IRC", KEY_UNBOUND, Category.CHAT, "cool people chat!",
				new SettingToggle("Supress Chat", false),
				new SettingToggle("Welcome", true),
				new SettingToggle("Show Channel", false));
    }



	public void onEnable() {
		if (!BleachFileMang.fileExists("IRCChannel.txt")) {
			BleachFileMang.createFile("IRCChannel.txt");
			BleachFileMang.appendFile("#epearlClient","IRCChannel.txt");
		}
		IRCManager.start();
		super.onEnable();
	}

	@Override
	public void onDisable() {
		super.onDisable();
		IRCManager.stop();
	}

	@Subscribe
	public void onPacketSend(EventSendPacket event) {
		if (event.getPacket() instanceof ChatMessageC2SPacket) {
			String text = ((ChatMessageC2SPacket) event.getPacket()).getChatMessage();
			if (text.startsWith("/") || text.startsWith(Command.PREFIX)) {
				return;
			} else if (text.startsWith("+pm ")) {
				String[] arrOfStr = text.substring(4).split(" ", 2);
				String target = arrOfStr[0];
				String message = arrOfStr[1];
				IRCManager.sendPrivateMessage(target, message);
				event.setCancelled(true);
				return;
			} else if (text.startsWith("+channel ")) {
				IRCManager.switchServer(text.substring(9));
				event.setCancelled(true);
				return;
			} else if (getSetting(0).asToggle().state) {
				IRCManager.sendMessage(text);
				event.setCancelled(true);
				return;
			} else if (text.startsWith("+")) {
				IRCManager.sendMessage(text.substring(1));
				event.setCancelled(true);
			}
		}
	}
	@Subscribe
	public void onPacketRead(EventReadPacket event) {
		if (event.getPacket() instanceof GameMessageS2CPacket && getSetting(0).asToggle().state) {
			event.setCancelled(true);
		}
	}
}
