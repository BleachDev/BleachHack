package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.CustomChat;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.file.BleachFileHelper;

import java.util.Arrays;

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
        return "customchat current | customchat reset | customchat prefix [prefix] | customchat suffix [suffix]";
    }

    @Override
    public void onCommand(String command, String[] args) throws Exception {
        if (args.length == 0) {
            BleachLogger.errorMessage(getSyntax());
            return;
        }

        CustomChat chat = (CustomChat) ModuleManager.getModule(CustomChat.class);
        if (args[0].equalsIgnoreCase("current")) {
            BleachLogger.infoMessage("Current prefix: \"" + chat.prefix + "\", suffix: \"" + chat.suffix + "\"");
        } else if (args[0].equalsIgnoreCase("reset")) {
            chat.prefix = "";
            chat.suffix = " \u25ba \u0432\u2113\u0454\u03b1c\u043d\u043d\u03b1c\u043a";

            BleachLogger.infoMessage("Reset the chat prefix and suffix");
            BleachFileHelper.saveMiscSetting("customChatPrefix", chat.prefix);
            BleachFileHelper.saveMiscSetting("customChatSuffix", chat.suffix);
        } else if (args[0].equalsIgnoreCase("prefix")) {
            chat.prefix = String.join(" ", Arrays.asList(args).subList(1, args.length)).trim() + " ";

            BleachLogger.infoMessage("Set prefix to: \"" + chat.prefix + "\"");
            BleachFileHelper.saveMiscSetting("customChatPrefix", chat.prefix);
        } else if (args[0].equalsIgnoreCase("suffix")) {
            chat.suffix = " " + String.join(" ", Arrays.asList(args).subList(1, args.length)).trim();

            BleachLogger.infoMessage("Set suffix to: \"" + chat.suffix + "\"");
            BleachFileHelper.saveMiscSetting("customChatSuffix", chat.suffix);
        } else {
            BleachLogger.errorMessage(getSyntax());
        }
    }

}