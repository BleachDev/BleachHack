package bleach.hack.module.mods;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.utils.BleachLogger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public class AutoAlign extends Module {

    public AutoAlign() {
        super("AutoAlign", KEY_UNBOUND, Category.MISC, "Resets your yaw and then disables.",
                new SettingMode("Mode ", "Auto", "Choose"),
                new SettingSlider("Yaw", -179, 180, -90, 0));
    }

    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        if (mc.world == null) return;

        if (getSetting(0).asMode().mode == 1) {
            player.yaw = (float) getSetting(1).asSlider().getValue();
        }
        else if (getSetting(0).asMode().mode == 0) {
            switch (determineHighway()) {
                case 1: player.yaw = -90; break;
                case 2: player.yaw = -45; break;
                case 3: player.yaw = -135; break;
                case 4: player.yaw = 90; break;
                case 5: player.yaw = 45; break;
                case 6: player.yaw = 135; break;
                case 7: player.yaw = 0; break;
                case 8: player.yaw = 180; break;
            }
        }
        ModuleManager.getModule(AutoAlign.class).toggle();
    }

    public int determineHighway() {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        int highwayNum = 0;
        if (player.getX() >= 100) {
            if (player.getZ() >= -5 && player.getZ() <= 5) {
                //+X highway
                highwayNum = 1;
            }
            else if (player.getZ() - player.getX() >= -50 && player.getZ() - player.getX() <= 50) {
                //+X+Z highway
                highwayNum = 2;
            }
            else if (player.getZ() + player.getX() >= -50 && player.getZ() + player.getX() <= 50) {
                //+X-Z highway
                highwayNum = 3;
            }
            else {
                BleachLogger.errorMessage("I have no idea where you are, but it probably isn't a highway.");
            }
        }
        else if (player.getX() <= -100) {
            if (player.getZ() >= -5 && player.getZ() <= 5) {
                //-X highway
                highwayNum = 4;
            }
            else if (player.getX() + player.getZ() >= -50 && player.getX() + player.getZ() <= 50) {
                //-X+Z highway
                highwayNum = 5;
            }
            else if (player.getZ() <= player.getX() + 100 && player.getZ() >= player.getX() - 100) {
                //-X-Z highway
                highwayNum = 6;
            }
            else {
                BleachLogger.errorMessage("I have no idea where you are, but it probably isn't a highway.");
            }
        }
        else if (player.getZ() >= 100) {
            if (player.getX() >= -5 && player.getX() <= 5) {
                //+Z highway
                highwayNum = 7;
            }
            else {
                BleachLogger.errorMessage("I have no idea where you are, but it probably isn't a highway.");
            }
        }
        else if (player.getZ() <= -100) {
            if (player.getX() >= -5 && player.getX() <= 5) {
                //-Z highway
                highwayNum = 8;
            }
            else {
                BleachLogger.errorMessage("I have no idea where you are, but it probably isn't a highway.");
            }
        }
        return highwayNum;
    }

}
