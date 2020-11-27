package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingToggle;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class Yaw extends Module {

    private boolean lDown = false;
    private boolean uDown = false;
    private boolean rDown = false;
    private boolean dDown = false;

    public Yaw() {
        super("Yaw", KEY_UNBOUND, Category.PLAYER, "Snaps your rotations to angles",
                new SettingToggle("Yaw", true).withDesc("Fixes your yaw").withChildren(
                        new SettingMode("Interval", "45", "30", "15", "90").withDesc("What angles to snap to")),
                new SettingToggle("Pitch", false).withDesc("Fixes your pitch").withChildren(
                        new SettingMode("Interval", "45", "30", "15", "90").withDesc("What angles to snap to")),
                new SettingToggle("Arrow Move", false).withDesc("Allows you to move between angles by using your arrow keys"),
                new SettingToggle("AutoAlign", false));
    }

    @Subscribe
    public void onTick(EventTick event) {
        /* yes looks like a good way to do it to me */
        if (getSetting(2).asToggle().state && mc.currentScreen == null) {
            int ymode = getSetting(0).asToggle().getChild(0).asMode().mode;
            int pmode = getSetting(1).asToggle().getChild(0).asMode().mode;

            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT) && !lDown) {
                mc.player.yaw -= ymode == 0 ? 45 : ymode == 1 ? 30 : ymode == 2 ? 15 : 90;
                lDown = true;
            } else if (!InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT)) {
                lDown = false;
            }

            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT) && !rDown) {
                mc.player.yaw += ymode == 0 ? 45 : ymode == 1 ? 30 : ymode == 2 ? 15 : 90;
                rDown = true;
            } else if (!InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT)) {
                rDown = false;
            }

            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_UP) && !uDown) {
                mc.player.pitch = MathHelper.clamp(mc.player.pitch - (pmode == 0 ? 45 : pmode == 1 ? 30 : pmode == 2 ? 15 : 90), -90, 90);
                uDown = true;
            } else if (!InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_UP)) {
                uDown = false;
            }

            if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_DOWN) && !dDown) {
                mc.player.pitch = MathHelper.clamp(mc.player.pitch + (pmode == 0 ? 45 : pmode == 1 ? 30 : pmode == 2 ? 15 : 90), -90, 90);
                dDown = true;
            } else if (!InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_DOWN)) {
                dDown = false;
            }
        }
        else if (getSetting(3).asToggle().state) {
            switch (determineHighway()) {
                case 1: mc.player.yaw = -90; break;
                case 2: mc.player.yaw = -45; break;
                case 3: mc.player.yaw = -135; break;
                case 4: mc.player.yaw = 90; break;
                case 5: mc.player.yaw = 45; break;
                case 6: mc.player.yaw = 135; break;
                case 7: mc.player.yaw = 0; break;
                case 8: mc.player.yaw = 180; break;
            }
        }

        snap();
    }

    public void snap() {
        // quic maff
        if (getSetting(0).asToggle().state) {
            int mode = getSetting(0).asToggle().getChild(0).asMode().mode;
            int interval = mode == 0 ? 45 : mode == 1 ? 30 : mode == 2 ? 15 : 90;
            int rot = (int) mc.player.yaw + (Math.floorMod((int) mc.player.yaw, interval) < interval / 2 ?
                    -Math.floorMod((int) mc.player.yaw, interval) : interval - Math.floorMod((int) mc.player.yaw, interval));

            mc.player.yaw = rot;
        }

        if (getSetting(1).asToggle().state) {
            int mode = getSetting(1).asToggle().getChild(0).asMode().mode;
            int interval = mode == 0 ? 45 : mode == 1 ? 30 : mode == 2 ? 15 : 90;
            int rot = MathHelper.clamp(((int) mc.player.pitch + (Math.floorMod((int) mc.player.pitch, interval) < interval / 2 ?
                    -Math.floorMod((int) mc.player.pitch, interval) : interval - Math.floorMod((int) mc.player.pitch, interval))), -90, 90);

            mc.player.pitch = rot;
        }
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
                highwayNum = -1;
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
                highwayNum = -1;
            }
        }
        else if (player.getZ() >= 100) {
            if (player.getX() >= -5 && player.getX() <= 5) {
                //+Z highway
                highwayNum = 7;
            }
            else {
                highwayNum = -1;
            }
        }
        else if (player.getZ() <= -100) {
            if (player.getX() >= -5 && player.getX() <= 5) {
                //-Z highway
                highwayNum = 8;
            }
            else {
                highwayNum = -1;
            }
        }
        return highwayNum;
    }
}