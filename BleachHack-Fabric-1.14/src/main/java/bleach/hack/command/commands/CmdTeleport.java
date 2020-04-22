/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
 * Copyright (c) 2019 Bleach.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.command.commands;

import bleach.hack.BleachHack;
import bleach.hack.command.Command;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Teleport;
import bleach.hack.utils.BleachLogger;
import net.minecraft.util.math.Vec3d;

import java.text.DecimalFormat;

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
        return "tp [(~)x] [(~)y] [(~)z] [blocks per teleport]";
    }

    DecimalFormat df = new DecimalFormat("#.###");

    public void onCommand(final String command, final String[] args) throws Exception {
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
                final double blocksPerTeleport = args.length == 3 ? 10000.0d : Double.valueOf(args[3]);
                Teleport.finalPos = new Vec3d(x, y, z);
                ModuleManager.getModule(Teleport.class).getSettings().get(0).toSlider().setValue(blocksPerTeleport);
                ModuleManager.getModule(Teleport.class).setToggled(true);
                BleachLogger.infoMessage("\n§aTeleporting to \n§cX: §b" + df.format(x) + "§a, \n§cY: §b" + df.format(y) + "§a, \n§cZ: §b" + df.format(z) + "\n§aat §b" + df.format(ModuleManager.getModule(Teleport.class).getSettings().get(0).toSlider().getValue()) + "§c blocks per teleport.");
            }
            catch (NullPointerException e){
                BleachLogger.warningMessage("Null Pointer Exception Caught!\nHonestly probably close MC.");
            }

        }
        else {
            BleachLogger.errorMessage(getSyntax());
        }
    }

}
