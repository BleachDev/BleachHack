package bleach.hack.command.commands;

import bleach.hack.command.Command;
import net.minecraft.text.LiteralText;

public class CmdDupe extends Command {

	@Override
	public String getAlias() {
		return "dupe";
	}

	@Override
	public String getDescription() {
		return "Dupes an item on vanilla servers. (PATCHED IN 1.14.4+)";
	}

	@Override
	public String getSyntax() {
		return ".dupe";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		mc.player.dropSelectedItem(true);
		mc.player.networkHandler.getConnection().disconnect(new LiteralText("Duping..."));
	}

}
