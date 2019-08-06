package bleach.hack.command.commands;

import java.util.List;

import bleach.hack.command.Command;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Notebot;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.file.BleachFileMang;
import bleach.hack.utils.file.BleachGithubReader;

public class CmdNotebot extends Command {

	private BleachGithubReader github = new BleachGithubReader();
	private BleachFileMang fileMang = new BleachFileMang();
	
	@Override
	public String getAlias() {
		return "notebot";
	}

	@Override
	public String getDescription() {
		return "Notebot Stuff";
	}

	@Override
	public String getSyntax() {
		return ".notebot load [file] | .notebot play | .notebot download";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if(args[0].equalsIgnoreCase("load")) {
			Notebot.filePath = args[1];
			BleachLogger.infoMessage("Set file to: " + args[1]);
		}else if(args[0].equalsIgnoreCase("play")) {
			ModuleManager.getModuleByName("Notebot").setToggled(false);
			ModuleManager.getModuleByName("Notebot").setToggled(true);
		}else if(args[0].equalsIgnoreCase("download")) {
			// TODO: not do this, its temporary i swear
			BleachLogger.infoMessage("Downloaded Songs.. Screen might lag");
			String s = "";
			List<String> l = github.readFileLines("notebot/blue.txt");
			fileMang.createFile("notebot/blue.txt", "");
			for(String l1: l) s += l1 + "\n";
			fileMang.appendFile("notebot/blue.txt", s); s = "";
			List<String> l2 = github.readFileLines("notebot/delfino-wwe.txt");
			fileMang.createFile("notebot/delfino-wwe.txt", "");
			for(String l3: l2) s += l3 + "\n";
			fileMang.appendFile("notebot/delfino-wwe.txt", s); s = "";
			List<String> l4 = github.readFileLines("notebot/despacito-wwe.txt");
			fileMang.createFile("notebot/despacito-wwe.txt", "");
			for(String l5: l4) s += l5 + "\n";
			fileMang.appendFile("notebot/despacito-wwe.txt", s); s = "";
			List<String> l6 = github.readFileLines("notebot/example.txt");
			fileMang.createFile("notebot/example.txt", "");
			for(String l7: l6) s += l7 + "\n";
			fileMang.appendFile("notebot/example.txt", s); s = "";
			List<String> l8 = github.readFileLines("notebot/running90s.txt");
			fileMang.createFile("notebot/running90s.txt", "");
			for(String l9: l8) s += l9 + "\n";
			fileMang.appendFile("notebot/running90s.txt", s); s = "";
			BleachLogger.infoMessage("Downloaded Songs: " + args[0]);
		}else {
			BleachLogger.errorMessage("Invalid command usage!");
			BleachLogger.infoMessage(getSyntax());
		}
	}

}
