package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.utils.BleachLogger;

public class CmdGuiReset extends Command {

	@Override
	public String getAlias() {
		return "guireset";
	}

	@Override
	public String getDescription() {
		return "Resets the clickgui windows";
	}

	@Override
	public String getSyntax() {
		return ".guireset";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		ClickGui.clickGui.resetGui();
		BleachLogger.infoMessage("Reset the clickgui!");
	}

}
