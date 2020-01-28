package bleach.hack.command.commands;

import java.util.List;

import bleach.hack.command.Command;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Nuker;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.file.BleachFileMang;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class CmdNuker extends Command {

	@Override
	public String getAlias() {
		return "nuker";
	}

	@Override
	public String getDescription() {
		return "Add/Remove Nuker Blocks";
	}

	@Override
	public String getSyntax() {
		return "nuker add [block] | .nuker remove [block]";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if(args[0].equalsIgnoreCase("add")) {
			if(Registry.BLOCK.get(new Identifier(args[1].toLowerCase())) == Blocks.AIR){
				BleachLogger.errorMessage("Invalid Block: " + args[1]);
				return;
			}
			
			BleachFileMang.appendFile(args[1].toLowerCase(), "nukerblocks.txt");
			ModuleManager.getModule(Nuker.class).toggle();
			ModuleManager.getModule(Nuker.class).toggle();
			BleachLogger.infoMessage("Added Block: " + args[1]);
			
		}else if(args[0].equalsIgnoreCase("remove")) {
			List<String> lines = BleachFileMang.readFileLines("nukerblocks.txt");
			
			if(lines.contains(args[1].toLowerCase())) {
				lines.remove(args[1].toLowerCase());
				
				String s = "";
				for(String s1: lines) s += s1 + "\n";
				
				BleachFileMang.createEmptyFile("nukerblocks.txt");
				BleachFileMang.appendFile(s, "nukerblocks.txt");
				
				BleachLogger.infoMessage("Removed Block: " + args[1]);
			}
			
			BleachLogger.errorMessage("Block Not In List: " + args[1]);
		}
	}

}
