package bleach.hack.command.commands;

import bleach.hack.command.Command;
import net.minecraft.client.Minecraft;

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
		Minecraft.getInstance().player.dropItem(true);
		Minecraft.getInstance().playerController.attackEntity(Minecraft.getInstance().player, Minecraft.getInstance().player);
	}

}
