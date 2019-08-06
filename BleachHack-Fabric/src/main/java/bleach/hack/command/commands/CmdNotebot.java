package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Notebot;
import bleach.hack.utils.BleachLogger;

public class CmdNotebot extends Command {

	@Override
	public String getAlias() {
		return "notebot";
	}

	@Override
	public String getDescription() {
		return "Notebot settings";
	}

	@Override
	public String getSyntax() {
		return ".notebot load [file] | .notebot play";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if(args[0].equalsIgnoreCase("load")) {
			Notebot.filePath = args[1];
			BleachLogger.infoMessage("Set file to: " + args[1]);
		}else if(args[0].equalsIgnoreCase("play")) {
			ModuleManager.getModuleByName("Notebot").setToggled(false);
			ModuleManager.getModuleByName("Notebot").setToggled(true);
		}
	}

}
