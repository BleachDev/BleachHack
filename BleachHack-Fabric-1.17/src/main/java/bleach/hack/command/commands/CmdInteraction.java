package bleach.hack.command.commands;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import bleach.hack.command.Command;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.EntityMenu;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.file.BleachFileHelper;

/**
 * @author <a href="https://github.com/lasnikprogram">Lasnik</a>
 */
public class CmdInteraction extends Command{

	@Override
	public String getAlias() {
		return "interaction";
	}

	@Override
	public String getDescription() {
		return "Manage the things which appear on the interaction screen. Use %name and %suggestion";
	}

	@Override
	public String getSyntax() {
		return "interaction add ( <alias> ) ( <command> ) | interaction remove ( <alias/command> ) | interection list | interaction clear";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		Map<String, String> interactions = ((EntityMenu) ModuleManager.getModule("EntityMenu")).interactions;

		if (args[0].equalsIgnoreCase("add")) {
			add(interactions, args);
		} else if (args[0].equalsIgnoreCase("remove")) {
			remove(interactions, args);
		} else if (args[0].equalsIgnoreCase("clear")) {
			clear(interactions);
		} else if (args[0].equalsIgnoreCase("list")) {
			list(interactions);
		} else {
			printSyntaxError();
		}
		
		JsonObject json = new JsonObject();
		for (Entry<String, String> entry: interactions.entrySet()) {
			json.add(entry.getKey(), new JsonPrimitive(entry.getValue()));
		}

		BleachFileHelper.saveMiscSetting("entityMenu", json);
	}

	private void add(Map<String, String> interactions, String[] args) {
		if (args.length < 7) {
			printSyntaxError();
			return;
		} 

		if (!args[1].equals("(")) {
			printSyntaxError("Don't forget the parentheses!");
			return;
		}

		String alias = "";
		String command = "";
		int interactiontart = -1;
		for (int i = 2; i < args.length; i++) {
			if (!args[i].equals(")")) {
				alias += args[i] + " ";
			} else {
				interactiontart = i + 1;
				break;
			}
		}

		if(!args[interactiontart].equals("(")){
			printSyntaxError("Don't forget the parentheses!");
			return;
		}

		for (int i = interactiontart + 1; i < args.length; i++) {
			if (!args[i].equals(")")) {
				command += args[i] + " ";
			}
		}

		if (alias.contains(":") || command.contains(":")) {
			printSyntaxError("Don't use the \":\" symbol");
		}

		if (interactions.size() <= 15) {
			interactions.put(alias.trim(), command.trim());
			BleachLogger.infoMessage("Added: " + alias.trim() + " : " + command.trim());
		} else {
			BleachLogger.errorMessage("You can't have more then 15 entrys. Remove one entry or clear all");
		}
	}

	private void remove(Map<String, String> interactions, String[] args) {
		if (args.length < 4) {
			printSyntaxError();
			return;
		}

		if (interactions.isEmpty()) {
			BleachLogger.errorMessage("Nothing to remove. You first have to add an entry");
			return;
		}

		if (!args[1].equals("(")) {
			printSyntaxError("Don't forget the parentheses!");
			return;
		}

		String result = "";
		for (int i = 2; i < args.length; i++) {
			if (!args[i].equals(")")) {
				result += args[i] + " ";
			}
		}

		System.out.println(result);
		for (Entry<String, String> s: interactions.entrySet()) {
			if (s.getKey().equalsIgnoreCase(result) || s.getValue().equalsIgnoreCase(result)) {
				interactions.remove(s.getKey());
				BleachLogger.infoMessage("Removed: " + s);
			}
		}
	}

	private void clear(Map<String, String> interactions) {
		interactions.clear();
		BleachLogger.infoMessage("Successfully cleared the list");
	}

	private void list(Map<String, String> interactions) {
		if (interactions.isEmpty()) {
			BleachLogger.errorMessage("Nothing to see here. You first have to add an entry");
			return;
		}

		for (Entry<String, String> s: interactions.entrySet()) {
			BleachLogger.infoMessage(s.getKey() + " : " + s.getValue());
		}
	}
}
