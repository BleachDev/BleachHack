package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.gui.NotebotScreen;
import bleach.hack.utils.BleachQueue;

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
		return "notebot";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		BleachQueue.queue.add(() -> mc.openScreen(new NotebotScreen()));
	}

}
