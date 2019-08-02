package bleach.hack.command.commands;

import bleach.hack.command.Command;

public class CmdDupe extends Command {

	@Override
	public String getAlias() {
		return "dupe";
	}

	@Override
	public String getDescription() {
		return "Dupes an item on vanilla servers.";
	}

	@Override
	public String getSyntax() {
		return ".dupe";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		mc.player.dropItem(true);
		mc.playerController.attackEntity(mc.player, mc.player);
	}

}
