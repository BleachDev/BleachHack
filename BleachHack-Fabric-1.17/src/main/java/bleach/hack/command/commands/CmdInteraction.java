package bleach.hack.command.commands;

import java.util.HashMap;

import bleach.hack.BleachHack;
import bleach.hack.command.Command;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.file.BleachFileHelper;

public class CmdInteraction extends Command{
	HashMap<String, String> interaction = BleachHack.interaction;
	
	@Override
	public String getAlias() {
		return "interaction";
	}

	@Override
	public String getDescription() {
		return "Manage the things which appear on the interaction screen. Use %name";
	}

	@Override
	public String getSyntax() {
		return "interaction add ( <alias> ) ( <command> ) | interaction remove ( <alias / command> ) | interection list | interaction clear";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		
		if (args[0].equalsIgnoreCase("add")) 
			add(args);
		else if (args[0].equalsIgnoreCase("remove"))
			remove(args);
		else if (args[0].equalsIgnoreCase("clear"))
			clear();
		else if (args[0].equalsIgnoreCase("list"))
			list();
		else 
			printSyntaxError();
		BleachFileHelper.SCHEDULE_SAVE_INTERACTION = true;
	}
	
	private void add (String[] args) {
		if (args.length < 3) {
			printSyntaxError("No alias or command specified");
			return;
		} 
		
		if (!args[1].equals("(")) {
			printSyntaxError("Don´t forget the parentheses!");
			return;
		}
		
		String alias = "", command = "";
		int interactiontart = -1;
		for (int i = 2; i < args.length; i++) {
			if (!args[i].equals(")"))
				alias += args[i] + " ";
			else {
				interactiontart = i + 1;
				break;
			}
		}
		
		if(!args[interactiontart].equals("(")){
			printSyntaxError("Don´t forget the parentheses!");
			return;
		}
		
		for (int i = interactiontart + 1; i < args.length; i++) 
			if (!args[i].equals(")"))
				command += args[i] + " ";
		
		if (alias.contains(":") || command.contains(":")) {
			printSyntaxError("Don´t use the \":\" symbol");
		}
		
		if (interaction.size() <= 15) {
			interaction.put(alias.trim(), command.trim());
			BleachLogger.infoMessage("Added: " + alias.trim() + " : " + command.trim());
		}
		else
			BleachLogger.errorMessage("You can´t have more then 15 entrys. Remove one entry or clear all");
	}
	
	private void remove (String[] args) {
		if (interaction.size() == 0) {
			BleachLogger.errorMessage("Nothing to remove. You first have to add an entry");
			return;
		}
		
		if(!args[1].equals("(")) {
			printSyntaxError("Don´t forget the parentheses!");
			return;
		}
		
		String result = "";
		
		for (int i = 2; i < args.length; i++) 
			if (!args[i].equals(")"))
				result += args[i] + " ";
		
		HashMap<String, String> map = BleachHack.interaction;
		for (String s: map.keySet())
			if (s.equals(result) || map.get(s).equals(result)) {
				map.remove(s);
				BleachLogger.infoMessage("Removed: " + s);
			}
	}
	
	private void clear () {
		interaction.clear();
		BleachLogger.infoMessage("Successfully cleared the list");
	}
	
	private void list () {
		if (interaction.size() == 0) {
			BleachLogger.errorMessage("Nothing to see here. You first have to add an entry");
			return;
		}
		
		for (String s: interaction.keySet())
			BleachLogger.infoMessage(s + " : " + interaction.get(s));
	}
}
