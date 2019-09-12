package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.command.CommandManager;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.file.BleachFileMang;

public class CmdPrefix extends Command {

	@Override
	public String getAlias() {
		return "prefix";
	}

	@Override
	public String getDescription() {
		return "Sets the command prefix";
	}

	@Override
	public String getSyntax() {
		return ".prefix [Char]";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if(args[0].isEmpty()) {
			BleachLogger.errorMessage("Prefix Cannot Be Empty");
			return;
		}
		
		BleachFileMang.createEmptyFile("prefix.txt");
		BleachFileMang.appendFile(args[0], "prefix.txt");
		CommandManager.prefix = args[0];
		BleachLogger.infoMessage("Set Prefix To: \"" + args[0] + "\"");
	}

}
