package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.utils.BleachLogger;
import net.minecraft.world.GameMode;

public class CmdGamemode extends Command {

	@Override
	public String getAlias() {
		return "gm";
	}

	@Override
	public String getDescription() {
		return "Sets clientside gamemode.";
	}

	@Override
	public String getSyntax() {
		return ".gm [0-3]";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		int gm;
		
		try {
			gm = Integer.parseInt(args[0]);
		} catch (Exception e) {
			BleachLogger.errorMessage("Unable to parse gamemode.");
			return;
		}
		
		if(gm == 0) {
			mc.player.setGameMode(GameMode.SURVIVAL);
			BleachLogger.infoMessage("Set gamemode to survival.");
		}else if(gm == 1) {
			mc.player.setGameMode(GameMode.CREATIVE);
			BleachLogger.infoMessage("Set gamemode to creative.");
		}else if(gm == 2) {
			mc.player.setGameMode(GameMode.ADVENTURE);
			BleachLogger.infoMessage("Set gamemode to adventure.");
		}else if(gm == 3) {
			mc.player.setGameMode(GameMode.SPECTATOR);
			BleachLogger.infoMessage("Set gamemode to spectator.");
		}else {
			BleachLogger.warningMessage("Unknown Gamemode Number.");
		}
	}

}
