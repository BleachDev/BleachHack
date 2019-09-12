package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.utils.BleachLogger;
import net.minecraft.client.util.InputUtil;

public class CmdBind extends Command {

	@Override
	public String getAlias() {
		return "bind";
	}

	@Override
	public String getDescription() {
		return "Binds a module";
	}

	@Override
	public String getSyntax() {
		return ".bind add [Module] [Key] | .bind del [Module]";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		for(Module m: ModuleManager.getModules()) {
			if(m.getName().equalsIgnoreCase(args[1])) {
				if(args[0].equalsIgnoreCase("add")) {
					m.setKey(InputUtil.fromName("key.keyboard." + args[2].toLowerCase()).getKeyCode());
					BleachLogger.infoMessage("Bound " + m.getName() + " To " + args[2]);
				}else if(args[0].equalsIgnoreCase("del")) {
					m.setKey(-1);
					BleachLogger.infoMessage("Removed Bind For " + m.getName());
				}
				return;
			}
		}
		BleachLogger.errorMessage("Could Not Find Module \"" + args[1] + "\"");
	}

}
