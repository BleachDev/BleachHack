package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.utils.BleachLogger;
import net.minecraft.entity.Entity;

public class CmdVanish extends Command {

	private static Entity vehicle;
    
    @Override
    public String getAlias() {
        return "vanish";
    }

    @Override
    public String getDescription() {
        return "Entity Desynchronisation.";
    }

    @Override
    public String getSyntax() {
        return ".vanish";
    }

    @Override
    public void onCommand(String command, String[] args) throws Exception {
        if (mc.player.getVehicle() != null && vehicle == null) {
            vehicle = mc.player.getVehicle();
            mc.player.stopRiding();
            mc.world.removeEntity(vehicle.getEntityId());
            BleachLogger.infoMessage("Vehicle " + vehicle.getName().asString() + " removed.");
        } else {
            if (vehicle != null) {
                vehicle.removed = false;
                mc.world.addEntity(vehicle.getEntityId(), vehicle);
                mc.player.startRiding(vehicle, true);
                BleachLogger.infoMessage("Vehicle created.");
                vehicle = null;
            } else {
                BleachLogger.errorMessage("No Vehicle.");
            }
        }
    }
}
