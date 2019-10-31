package bleach.hack.module.mods;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.utils.BleachLogger;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.packet.DisconnectS2CPacket;
import net.minecraft.client.network.packet.LoginHelloS2CPacket;
import net.minecraft.client.network.packet.PlayerRespawnS2CPacket;
import net.minecraft.util.math.Vec3d;

public class Teleport extends Module {

    private boolean alreadyDone;
    private long lastTp;
    private Vec3d lastPos;

    public Teleport() {
        super("Teleport", -1, Category.MISC, "What are you doing here?",
                new SettingSlider("X: ", -30000000.0d, 30000000.0d, 0.0d, 5),
                new SettingSlider("Y: ", -100.0d, 10000.0d, 0.0d, 5),
                new SettingSlider("Z: ", -30000000.0d, 30000000.0d, 0.0d, 5),
                new SettingSlider("BPT: ", 0.00001d, 1000, 100.0d, 5));
    }


    @Override
    public void onDisable() {
        super.onDisable();
        alreadyDone = false;
    }

    @Subscribe
    public void readPacket(EventReadPacket event) {
        if (event.getPacket() instanceof LoginHelloS2CPacket || event.getPacket() instanceof DisconnectS2CPacket || event.getPacket() instanceof PlayerRespawnS2CPacket) {
            alreadyDone = false;
            getSettings().get(0).toSlider().setValue(0.0d);
            getSettings().get(1).toSlider().setValue(0.0d);
            getSettings().get(2).toSlider().setValue(0.0d);
            getSettings().get(3).toSlider().setValue(100.0d);
            BleachLogger.infoMessage("Teleport Ended 1");
            setToggled(false);
            BleachHack.eventBus.unregister(this);
        }
    }

    @Subscribe
    public void onTick(EventTick event) {
        if (!alreadyDone) {
            BleachLogger.infoMessage("\n§9Teleporting to \n§bX: §c" + ModuleManager.getModule(Teleport.class).getSettings().get(0).toSlider().getValue() + ", \n§bY: §c" + ModuleManager.getModule(Teleport.class).getSettings().get(1).toSlider().getValue() + ", \n§bZ: §c" + ModuleManager.getModule(Teleport.class).getSettings().get(2).toSlider().getValue() + "\n§bat §c" + ModuleManager.getModule(Teleport.class).getSettings().get(3).toSlider().getValue() + "§b blocks per teleport.");
            alreadyDone = true;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        Vec3d targetVec = new Vec3d(getSettings().get(0).toSlider().getValue(), getSettings().get(1).toSlider().getValue(), getSettings().get(2).toSlider().getValue());

        Vec3d tpDirectionVec = targetVec.subtract(mc.player.getPosVector()).normalize();
        int chunkX = (int) Math.floor(mc.player.getPosVector().x / 16.0D);
        int chunkZ = (int) Math.floor(mc.player.getPosVector().z / 16.0D);
        if (mc.world.isChunkLoaded(chunkX, chunkZ)) {
            lastPos = mc.player.getPosVector();
            if (targetVec.distanceTo(mc.player.getPosVector()) < 0.5D || getSettings().get(3).toSlider().getValue() == 0) {
                alreadyDone = false;
                getSettings().get(0).toSlider().setValue(0.0d);
                getSettings().get(1).toSlider().setValue(0.0d);
                getSettings().get(2).toSlider().setValue(0.0d);
                getSettings().get(3).toSlider().setValue(100.0d);
                BleachLogger.infoMessage("Teleport Ended 2");
                setToggled(false);
                BleachHack.eventBus.unregister(this);
            } else {
                mc.player.setVelocity(0,0,0);
            }
            if (targetVec.distanceTo(mc.player.getPosVector()) >= getSettings().get(3).toSlider().getValue()) {
                final Vec3d vec = tpDirectionVec.multiply(getSettings().get(3).toSlider().getValue());
                mc.player.setPosition(mc.player.getPos().getX() + vec.getX(), mc.player.getPos().getY() + vec.getY(), mc.player.getPos().getZ() + vec.getZ());
            } else {
                final Vec3d vec = tpDirectionVec.multiply(targetVec.distanceTo(mc.player.getPosVector()));
                mc.player.setPosition(mc.player.getPosVector().getX() + vec.x, mc.player.getPosVector().getY() + vec.y, mc.player.getPosVector().getZ() + vec.z);
            }
            lastTp = System.currentTimeMillis();
        } else if (lastTp + 2000L < System.currentTimeMillis()) {
            mc.player.setPosition(lastPos.x, lastPos.y, lastPos.z);
        }

    }

}
