package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.gui.EntityMenuEditScreen;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.EntityMenu;
import bleach.hack.util.BleachQueue;
import bleach.hack.util.PairList;

/**
 * @author <a href="https://github.com/lasnikprogram">Lasnik</a>
 */
public class CmdEntityMenu extends Command{

	@Override
	public String getAlias() {
		return "entitymenu";
	}

	@Override
	public String getDescription() {
		return "Opens the gui to manage the things which appear on the interaction screen.";
	}

	@Override
	public String getSyntax() {
		return "entitymenu";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		PairList<String, String> interactions = ((EntityMenu) ModuleManager.getModule("EntityMenu")).interactions;
		
		BleachQueue.add(() -> mc.openScreen(new EntityMenuEditScreen(interactions)));
	}
}
