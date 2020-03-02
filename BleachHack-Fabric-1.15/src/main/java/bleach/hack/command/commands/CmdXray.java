package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Xray;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.file.BleachFileMang;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class CmdXray extends Command {
    //brb stealing your nuker command
    @Override
    public String getAlias() {
        return "xray";
    }

    @Override
    public String getDescription() {
        return "Edit Xray blocks";
    }

    @Override
    public String getSyntax() {
        return "xray add [block] | .xray remove [block] | .xray clear | .xray list";
    }

    @Override
    public void onCommand(String command, String[] args) throws Exception {
        if(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
        	
        	Xray xray = (Xray) ModuleManager.getModule(Xray.class);
            String block = (args[1].contains(":") ? "" : "minecraft:") + args[1].toLowerCase();
            
	        if (args[0].equalsIgnoreCase("add")) {
	            if (Registry.BLOCK.get(new Identifier(block)) == Blocks.AIR) {
	                BleachLogger.errorMessage("Invalid Block: " + args[1]);
	                return;
	            } else if(xray.getVisibleBlocks().contains(Registry.BLOCK.get(new Identifier(block)))) {
	                BleachLogger.errorMessage("Block is already added!");
	                return;
	            }
	            
	            BleachFileMang.appendFile(block, "xrayblocks.txt");
	            xray.toggle();
	            xray.toggle();
	            BleachLogger.infoMessage("Added Block: " + args[1]);
	
	        } else if (args[0].equalsIgnoreCase("remove")) {
	            List<String> lines = BleachFileMang.readFileLines("xrayblocks.txt");
	
	            if (lines.contains(block)) {
	                lines.remove(block);
	
	                String s = "";
	                for (String s1 : lines) s += s1 + "\n";
	
	                BleachFileMang.createEmptyFile("xrayblocks.txt");
	                BleachFileMang.appendFile(s, "xrayblocks.txt");
	                xray.toggle();
	                xray.toggle();
	                BleachLogger.infoMessage("Removed Block: " + args[1]);
	            } else {
	                BleachLogger.errorMessage("Block Not In List: " + args[1]);
	            }
	        }
        } else if(args[0].equalsIgnoreCase("clear")) {
			BleachFileMang.createEmptyFile("xrayblocks.txt");
			BleachLogger.infoMessage("Cleared Xray Blocks");
		} else if (args[0].equalsIgnoreCase("list")) {
        	List<String> lines = BleachFileMang.readFileLines("xrayblocks.txt");
			
			String s = "";
			for(String l: lines) {
				s += "§6" + l + "\n";
			}
			
			BleachLogger.infoMessage(s);
        }
    }
}

