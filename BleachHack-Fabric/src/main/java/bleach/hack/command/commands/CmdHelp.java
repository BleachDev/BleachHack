package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.command.CommandManager;
import bleach.hack.utils.BleachLogger;

public class CmdHelp extends Command {

	@Override
	public String getAlias() {
		return "help";
	}

	@Override
	public String getDescription() {
		return "Displays all the commands";
	}

	@Override
	public String getSyntax() {
		return "help / .help [Command]";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		String cmd = null;
		try { cmd = args[0]; }catch(Exception e) {}
		
		for(Command c: CommandManager.getCommands()) {
			if(!cmd.isEmpty() && !cmd.equalsIgnoreCase(c.getAlias())) continue;
			BleachLogger.noPrefixMessage("§5." + c.getAlias() + " | §6" + c.getDescription() + " | §e" + c.getSyntax());
		}
	}

}
