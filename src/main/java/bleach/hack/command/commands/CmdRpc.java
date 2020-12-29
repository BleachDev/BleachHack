package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.DiscordRPCMod;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.file.BleachFileHelper;

public class CmdRpc extends Command {

	@Override
	public String getAlias() {
		return "rpc";
	}

	@Override
	public String getDescription() {
		return "Sets custom discord rpc text";
	}

	@Override
	public String getSyntax() {
		return "rpc [top text] [bottom text]";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if (args.length != 2) {
			BleachLogger.errorMessage(getSyntax());
		}

		ModuleManager.getModule(DiscordRPCMod.class).setText(args[0], args[1]);

		BleachLogger.infoMessage("Set RPC to " + args[0] + ", " + args[1]);

		BleachFileHelper.saveMiscSetting("discordRPCTopText", args[0]);
		BleachFileHelper.saveMiscSetting("discordRPCBottomText", args[1]);
	}

}
