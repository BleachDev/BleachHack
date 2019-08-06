package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.utils.BleachLogger;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;

public class CmdRename extends Command {

	@Override
	public String getAlias() {
		return "rename";
	}

	@Override
	public String getDescription() {
		return "Renames an item, use \"&\" for color";
	}

	@Override
	public String getSyntax() {
		return ".rename [name]";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if(!mc.player.abilities.creativeMode) {
			BleachLogger.errorMessage("Not In Creative Mode!");
			return;
		}
		
		ItemStack i = mc.player.inventory.getMainHandStack();
		
		String name = "";
		for(int j = 0; j < args.length; j++) name += args[j] += " ";
		
		i.setCustomName(new LiteralText(name.replace("&", "§").replace("§§", "&")));
		BleachLogger.infoMessage("Renamed Item");
	}

}
