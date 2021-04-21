package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.command.CommandManager;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

public class CmdSay extends Command {

	public CmdSay() {
		super("say", "Says a message in chat.", "say <message>", CommandCategory.MISC);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		CommandManager.allowNextMsg = true;
		mc.player.networkHandler.sendPacket(new ChatMessageC2SPacket(String.join(" ", args)));
	}

}
