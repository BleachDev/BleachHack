package bleach.hack.command;

import java.util.Arrays;
import java.util.List;

import bleach.hack.command.commands.*;
import bleach.hack.utils.BleachLogger;

public class CommandManager {

	private List<Command> commands = Arrays.asList(
			new CmdGamemode(),
			new CmdGuiReset(),
			new CmdHelp(),
			new CmdPeek(),
			new CmdToggle());
	
	public List<Command> getCommands(){
		return commands;
	}
	
	public void callCommand(String input) {
		String[] split = input.split(" ");
		String command = split[0];
		String args = input.substring(command.length()).trim();
		for(Command c: getCommands()) {
			if(c.getAlias().equalsIgnoreCase(command)) {
				try {
					c.onCommand(command, args.split(" "));
				}catch(Exception e) {
					e.printStackTrace();
					BleachLogger.errorMessage("Invalid command usage!");
					BleachLogger.infoMessage(c.getSyntax());
				}
				return;
			}
		}
		BleachLogger.errorMessage("Command Not Found, Maybe Try .Help");
	}
}
