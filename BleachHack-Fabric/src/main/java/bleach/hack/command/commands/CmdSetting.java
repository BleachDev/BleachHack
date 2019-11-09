package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.utils.BleachLogger;

public class CmdSetting extends Command {

	@Override
	public String getAlias() {
		return "setting";
	}

	@Override
	public String getDescription() {
		return "Changes a setting in a module";
	}

	@Override
	public String getSyntax() {
		return ".setting [Module] [Setting number (starts at 0)] [value]";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if(args.length < 2) {
			BleachLogger.errorMessage(getSyntax());
			return;
		}
		
		Module m = ModuleManager.getModuleByName(args[0]);
		SettingBase s = m.getSettings().get(Integer.parseInt(args[1]));
		
		if(s instanceof SettingSlider) s.toSlider().setValue(Double.parseDouble(args[2]));
		else if(s instanceof SettingToggle) s.toToggle().state = Boolean.valueOf(args[2]);
		else if(s instanceof SettingMode) s.toMode().mode = Integer.parseInt(args[2]);
		else {
			BleachLogger.errorMessage("Invalid Command");
			return;
		}
		
		BleachLogger.infoMessage("Set Setting " + args[1] + " Of " + m.getName() + " To " + args[2]);
	}

}
