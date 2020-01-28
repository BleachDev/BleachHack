package bleach.hack.command.commands;

import java.util.List;

import bleach.hack.command.Command;
import bleach.hack.gui.NotebotScreen;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.BleachQueue;
import bleach.hack.utils.Midi2Notebot;
import bleach.hack.utils.file.BleachFileMang;

public class CmdNotebot extends Command {
	
	@Override
	public String getAlias() {
		return "notebot";
	}

	@Override
	public String getDescription() {
		return "Shows the notebot gui";
	}

	@Override
	public String getSyntax() {
		return ".notebot | .notebot convert (file in .minecraft/bleach/)";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if(args.length >= 2 && args[0].equalsIgnoreCase("convert")) {
			int i = 0;
			String s = "";
			List<List<Integer>> notes = Midi2Notebot.convert(BleachFileMang.stringsToPath(args[1]));

			while(BleachFileMang.fileExists("notebot", "notebot" + i + ".txt")) i++;
			for(List<Integer> i1: notes) s += i1.get(0) + ":" + i1.get(1) + ":" + i1.get(2) + "\n";
			BleachFileMang.appendFile(s, "notebot", "notebot" + i + ".txt");
			BleachLogger.infoMessage("Saved Song As: notebot" + i + ".txt [" + notes.size() + " Notes]");
		}else {
			BleachQueue.queue.add(() -> mc.openScreen(new NotebotScreen()));
		}
	}

}
