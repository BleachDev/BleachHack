package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.gui.ConfigSettingsScreen;
import bleach.hack.utils.BleachQueue;

public class CmdConfigSettings extends Command {

	@Override
	public String getAlias() {
		return "configsettings";
	}

	@Override
	public String getDescription() {
		return "Settings for the config";
	}

	@Override
	public String getSyntax() {
		return "configsettings";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		BleachQueue.add(() -> mc.openScreen(new ConfigSettingsScreen()));
	}

}