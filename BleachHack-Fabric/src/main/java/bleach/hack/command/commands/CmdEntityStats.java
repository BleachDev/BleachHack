package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.utils.BleachLogger;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.*;

public class CmdEntityStats extends Command {

    private String maxHealth;
    private String speed;
    private String jumpHeight;

    @Override
    public String getAlias() {
        return "estats";
    }

    @Override
    public String getDescription() {
        return "Get stats of vehicle entity.";
    }

    @Override
    public String getSyntax() {
        return "estats";
    }

    @Override
    public void onCommand(String command, String[] args) throws Exception {
        if (mc.player.getVehicle() != null) {
            if     (mc.player.getVehicle() instanceof HorseEntity ||
                    mc.player.getVehicle() instanceof DonkeyEntity ||
                    mc.player.getVehicle() instanceof LlamaEntity ||
                    mc.player.getVehicle() instanceof MuleEntity) {
                HorseBaseEntity h = (HorseBaseEntity) mc.player.getVehicle();
                maxHealth = "" + h.getHealthMaximum();
                speed = "" + (int) (1000 * h.getMovementSpeed());
                jumpHeight = "" + (float) (1.25219 * (1 + h.getJumpStrength()));
                BleachLogger.infoMessage("\n§6Entity Stats:\n§cMax Health: §b" + maxHealth + "\n§cSpeed: §b" + speed + "\n§cJump: §b" + jumpHeight);
            }else if (mc.player.getVehicle() instanceof LivingEntity) {
                LivingEntity l = (LivingEntity) mc.player.getVehicle();
                maxHealth = "" + l.getHealthMaximum();
                speed = "" + (int) (1000 * l.getMovementSpeed());
                BleachLogger.infoMessage("\n§6Entity Stats:\n§cMax Health: §b" + maxHealth + "\n§cSpeed: §b" + speed);
            }
        }else {
            BleachLogger.errorMessage("Not riding a living entity.");
        }
    }
}
