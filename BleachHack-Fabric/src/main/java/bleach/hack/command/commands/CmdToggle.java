package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.utils.BleachLogger;

public class CmdToggle extends Command {

	@Override
	public String getAlias() {
		return "toggle";
	}

	@Override
	public String getDescription() {
		return "Toggles a mod with a command.";
	}

	@Override
	public String getSyntax() {
		return ".toggle [Module]";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		for(Module m: ModuleManager.getModules()) {
			if(args[0].equalsIgnoreCase(m.getName())) {
				m.toggle();
				BleachLogger.infoMessage(m.getName() + " Toggled");
				return;
			}
		}
		BleachLogger.errorMessage("Module \"" + args[0] + "\" Not Found!");
	}

}
