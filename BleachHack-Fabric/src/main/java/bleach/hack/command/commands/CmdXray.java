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
        return "Add/Remove Xray blocks";
    }

    @Override
    public String getSyntax() {
        return ".xray add [block] | .xray remove [block]";
    }

    @Override
    public void onCommand(String command, String[] args) throws Exception {
        Xray xray = (Xray) ModuleManager.getModule(Xray.class);
        if (args[0].equalsIgnoreCase("add")) {
            if (Registry.BLOCK.get(new Identifier(args[1].toLowerCase())) == Blocks.AIR) {
                BleachLogger.errorMessage("Invalid Block: " + args[1]);
                return;
            } else if (xray.isVisible(Registry.BLOCK.get(new Identifier(args[1].toLowerCase())))) {
                BleachLogger.errorMessage("Block is already added!");
                return;
            }
            BleachFileMang.appendFile(args[1].toLowerCase(), "xrayblocks.txt");
            xray.toggle();
            xray.toggle();
            BleachLogger.infoMessage("Added Block: " + args[1]);

        } else if (args[0].equalsIgnoreCase("remove")) {
            List<String> lines = BleachFileMang.readFileLines("xrayblocks.txt");

            if (lines.contains(args[1].toLowerCase())) {
                lines.remove(args[1].toLowerCase());

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
    }
}

