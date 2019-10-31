package bleach.hack.command;

import java.util.Arrays;
import java.util.List;

import bleach.hack.command.commands.*;
import bleach.hack.utils.BleachLogger;

public class CommandManager {

	public static String prefix = ".";
	
	private static List<Command> commands = Arrays.asList(
			new CmdBind(),
			new CmdDupe(),
			new CmdEnchant(),
			new CmdGamemode(),
			new CmdGive(),
			new CmdGuiReset(),
			new CmdHelp(),
			new CmdNotebot(),
			new CmdNuker(),
			new CmdPeek(),
			new CmdPrefix(),
			new CmdRbook(),
			new CmdRename(),
			new CmdSkull(),
			new CmdTeleport(),
			new CmdToggle(),
			new CmdVanish(),
			new CmdXray());
	
	public static List<Command> getCommands(){
		return commands;
	}
	
	public static void callCommand(String input) {
		String[] split = input.split(" ");
		System.out.println(Arrays.asList(split));
		String command = split[0];
		String args = input.substring(command.length()).trim();
		for(Command c: getCommands()) {
			if(c.getAlias().equalsIgnoreCase(command)) {
				try {
					c.onCommand(command, args.split(" "));
				}catch(Exception e) {
					e.printStackTrace();
					BleachLogger.errorMessage("Invalid Syntax!");
					BleachLogger.infoMessage(c.getSyntax());
				}
				return;
			}
		}
		BleachLogger.errorMessage("Command Not Found, Maybe Try .Help");
	}
}
