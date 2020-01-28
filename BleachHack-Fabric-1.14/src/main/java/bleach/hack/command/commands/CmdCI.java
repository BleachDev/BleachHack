package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.utils.BleachLogger;
import net.minecraft.item.ItemStack;
import net.minecraft.world.GameMode;

public class CmdCI extends Command {

    @Override
    public String getAlias() {
        return "ci";
    }

    @Override
    public String getDescription() {
        return "Clears inventory (Creative)";
    }

    @Override
    public String getSyntax() {
        return "ci";
    }

    @Override
    public void onCommand(String command, String[] args) throws Exception {
        for (int i = 0; i < 200; i++) {
            if (mc.interactionManager.getCurrentGameMode() == GameMode.CREATIVE) {
                mc.player.inventory.setInvStack(i, new ItemStack(null));
            } else {
                BleachLogger.errorMessage("Bruh you're not in creative.");
                return;
            }
        }
        BleachLogger.infoMessage("Cleared all items");
    }

}
