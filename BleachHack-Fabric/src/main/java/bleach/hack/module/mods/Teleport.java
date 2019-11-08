package bleach.hack.module.mods;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.EntityUtils;

import com.google.common.eventbus.Subscribe;
import net.minecraft.server.network.packet.LoginHelloC2SPacket;
import net.minecraft.util.math.Vec3d;

public class Teleport extends Module {

	private long lastTp;
    private Vec3d lastPos;
    public static Vec3d finalPos;

    public Teleport() {
        super("Teleport", -1, Category.MISC, "What are you doing here?",
            new SettingSlider("BPT: ", 0.01, 20, 1, 2),
            new SettingToggle("TP Exploit", false));
    }

    @Subscribe
    public void sendPacket(EventSendPacket event) {
        if (event.getPacket() instanceof LoginHelloC2SPacket) {
            setToggled(false);
        }
    }

    @Subscribe
    public void onTick(EventTick event) {
    	if(finalPos == null) {
    		BleachLogger.errorMessage("Position not set, use .tp");
    		setToggled(false);
    		return;
    	}
        
        if (getSettings().get(1).toToggle().state) {
            mc.player.setPosition(finalPos.getX(), finalPos.getY(), finalPos.getZ());
            ModuleManager.getModule(BookCrash.class).setToggled(true);
        } else {
        	EntityUtils.facePos(finalPos.x, finalPos.y, finalPos.z);
        	
            Vec3d nextStep = new Vec3d(0, 0, Math.min(finalPos.distanceTo(mc.player.getPosVector()), getSettings().get(0).toSlider().getValue()))
    				.rotateX(-(float) Math.toRadians(mc.player.pitch))
    				.rotateY(-(float) Math.toRadians(mc.player.yaw));
            
            int chunkX = (int) Math.floor(mc.player.getPosVector().x / 16.0D);
            int chunkZ = (int) Math.floor(mc.player.getPosVector().z / 16.0D);
            if (mc.world.isChunkLoaded(chunkX, chunkZ)) {
                lastPos = mc.player.getPosVector();
                if (finalPos.distanceTo(mc.player.getPosVector()) < 0.3 || getSettings().get(0).toSlider().getValue() == 0) {
                    setToggled(false);
                }

                if (finalPos.distanceTo(mc.player.getPosVector()) >= Math.max(1, getSettings().get(0).toSlider().getValue())) {
                    mc.player.setPosition(mc.player.x + nextStep.x, mc.player.y + nextStep.y, mc.player.z + nextStep.z);
                } else {
                    mc.player.setPosition(finalPos.x, finalPos.y, finalPos.z);
                }
                lastTp = System.currentTimeMillis();
            } else if (lastTp + 2000L > System.currentTimeMillis()) {
                mc.player.setPosition(lastPos.x, lastPos.y, lastPos.z);
            }
        }

    }

}
