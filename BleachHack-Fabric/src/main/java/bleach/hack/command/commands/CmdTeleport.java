package bleach.hack.command.commands;

import bleach.hack.BleachHack;
import bleach.hack.command.Command;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Teleport;
import bleach.hack.utils.BleachLogger;
import net.minecraft.client.*;

public class CmdTeleport extends Command {

    @Override
    public String getAlias() {
        return "tp";
    }

    @Override
    public String getDescription() {
        return "Teleport yourself at\nspecified speed.";
    }

    @Override
    public String getSyntax() {
        return ".tp [(~)x] [(~)y] [(~)z] [blocks per teleport]";
    }

    public void onCommand(final String command, final String[] args) throws Exception {
        final MinecraftClient mc = MinecraftClient.getInstance();
        if (args[0].equalsIgnoreCase("stop")) {
            BleachLogger.warningMessage("Teleport Cancelled!");
            BleachHack.eventBus.unregister(ModuleManager.getModule(Teleport.class));
            return;
        }
        if (args.length >= 3) {
            try {
                final double x = args[0].equals("~") ? mc.player.getPos().getX() : args[0].charAt(0) == '~' ? Double.parseDouble(args[0].substring(1)) + mc.player.getPos().getX() : Double.parseDouble(args[0]);
                final double y = args[1].equals("~") ? mc.player.getPos().getY() : args[1].charAt(0) == '~' ? Double.parseDouble(args[1].substring(1)) + mc.player.getPos().getY() : Double.parseDouble(args[1]);
                final double z = args[2].equals("~") ? mc.player.getPos().getZ() : args[2].charAt(0) == '~' ? Double.parseDouble(args[2].substring(1)) + mc.player.getPos().getZ() : Double.parseDouble(args[2]);
                final double blocksPerTeleport = args.length == 3 ? 1.0d : Double.valueOf(args[3]);
                ModuleManager.getModule(Teleport.class).getSettings().get(0).toSlider().setValue(x);
                ModuleManager.getModule(Teleport.class).getSettings().get(1).toSlider().setValue(y);
                ModuleManager.getModule(Teleport.class).getSettings().get(2).toSlider().setValue(z);
                ModuleManager.getModule(Teleport.class).getSettings().get(3).toSlider().setValue(blocksPerTeleport);
                ModuleManager.getModule(Teleport.class).setToggled(true);
            }
            catch (NullPointerException e){
                BleachLogger.warningMessage("Null Pointer Exception Caught!\nHonestly probably close MC.");
            }

        }
        else{
            BleachLogger.noPrefixMessage(".tp [(~)x] [(~)y] [(~)z] [blocks per teleport]");
        }
        return;
    }

}