package bleach.hack.command.commands;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonPrimitive;

import bleach.hack.command.Command;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.CustomChat;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.file.BleachFileHelper;

public class CmdCustomChat extends Command {

	@Override
	public String getAlias() {
		return "customchat";
	}

	@Override
	public String getDescription() {
		return "Changes customchat prefix and suffix";
	}

	@Override
	public String getSyntax() {
		return "customchat current | customchat reset | customchat prefix <prefix> | customchat suffix <suffix>";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if (args.length == 0) {
			printSyntaxError();
			return;
		}

		CustomChat chat = (CustomChat) ModuleManager.getModule("CustomChat");

		if (args[0].equalsIgnoreCase("current")) {
			BleachLogger.infoMessage("Current prefix: \"" + chat.prefix + "\", suffix: \"" + chat.suffix + "\"");
		} else if (args[0].equalsIgnoreCase("reset")) {
			chat.prefix = "";
			chat.suffix = " \u25ba \u0432\u2113\u0454\u03b1c\u043d\u043d\u03b1c\u043a";

			BleachFileHelper.saveMiscSetting("customChatPrefix", new JsonPrimitive(chat.prefix));
			BleachFileHelper.saveMiscSetting("customChatSuffix", new JsonPrimitive(chat.suffix));
			BleachLogger.infoMessage("Reset the chat prefix and suffix");
		} else if (args[0].equalsIgnoreCase("prefix")) {
			chat.prefix = String.join(" ", ArrayUtils.subarray(args, 1, args.length)).trim() + " ";

			BleachFileHelper.saveMiscSetting("customChatPrefix", new JsonPrimitive(chat.prefix));
			BleachLogger.infoMessage("Set prefix to: \"" + chat.prefix + "\"");
		} else if (args[0].equalsIgnoreCase("suffix")) {
			chat.suffix = " " + String.join(" ", ArrayUtils.subarray(args, 1, args.length)).trim();

			BleachFileHelper.saveMiscSetting("customChatSuffix", new JsonPrimitive(chat.suffix));
			BleachLogger.infoMessage("Set suffix to: \"" + chat.suffix + "\"");
		} else {
			printSyntaxError();
		}
	}

}