package bleach.hack.command.commands;

import bleach.hack.BleachHack;
import bleach.hack.command.Command;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.file.BleachFileHelper;

public class CmdFriends extends Command {

    @Override
    public String getAlias() {
        return "friend";
    }

    @Override
    public String getDescription() {
        return "Manage friends";
    }

    @Override
    public String getSyntax() {
        return "friend add [user] | friend del [user] | friend remove [user] | friend list | friend clear";
    }

    @Override
    public void onCommand(String command, String[] args) throws Exception {
        if (args[0].equalsIgnoreCase("add")) {
            if (args.length < 2) {
                BleachLogger.errorMessage("No username selected");
                BleachLogger.errorMessage(getSyntax());
                return;
            }

            BleachHack.friendMang.add(args[1]);
            BleachLogger.infoMessage("Added \"" + args[1] + "\" to the friend list");
        } else if (args[0].equalsIgnoreCase("del")) {
            if (args.length < 2) {
                BleachLogger.errorMessage("No username selected");
                BleachLogger.errorMessage(getSyntax());
                return;
            }

            BleachHack.friendMang.remove(args[1].toLowerCase());
            BleachLogger.infoMessage("Deleted \"" + args[1] + "\" from the friend list");
        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                BleachLogger.errorMessage("No username selected");
                BleachLogger.errorMessage(getSyntax());
                return;
            }

            BleachHack.friendMang.remove(args[1].toLowerCase());
            BleachLogger.infoMessage("Removed \"" + args[1] + "\" from the friend list");
        } else if (args[0].equalsIgnoreCase("list")) {
            String text = "";

            for (String f : BleachHack.friendMang.getFriends()) {
                text += "\n\u00a72" + f;
            }

            BleachLogger.infoMessage(text);
        } else if (args[0].equalsIgnoreCase("clear")) {
            BleachHack.friendMang.getFriends().clear();

            BleachLogger.infoMessage("Cleared Friend list");
        } else {
            BleachLogger.errorMessage("Invalid Syntax!\n" + getSyntax());
        }

        BleachFileHelper.SCHEDULE_SAVE_FRIENDS = true;
    }

}
