package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.command.CommandManager;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

public class CmdSay extends Command {

	@Override
	public String getAlias() {
		return "say";
	}

	@Override
	public String getDescription() {
		return "Says a message in chat";
	}

	@Override
	public String getSyntax() {
		return "say <message>";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		CommandManager.allowNextMsg = true;
		mc.player.networkHandler.sendPacket(new ChatMessageC2SPacket(String.join(" ", args)));
	}

}
