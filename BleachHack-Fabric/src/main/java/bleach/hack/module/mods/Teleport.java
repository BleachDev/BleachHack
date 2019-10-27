package bleach.hack.module.mods;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.utils.BleachLogger;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.packet.*;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;

public class Teleport extends Module {

    private boolean alreadyDone;
    private long lastTp;
    private Vec3d lastPos;
    private double toX;
    private double toY;
    private double toZ;
    private double remainder;

    public Teleport() {
        super("Teleport", -1, Category.MISC, "What are you doing here?",
            new SettingSlider(0.0d, Double.POSITIVE_INFINITY, 0.0d, 5, "X: "),
            new SettingSlider(0.0d, Double.POSITIVE_INFINITY, 0.0d, 5, "X: "),
            new SettingSlider(0.0d, Double.POSITIVE_INFINITY, 0.0d, 5, "Y: "),
            new SettingSlider(0.0d, Double.POSITIVE_INFINITY, 0.0d, 5, "Z: "),
            new SettingSlider(0.00001d, Double.POSITIVE_INFINITY, 1.0d, 5, "BPT: "),
            new SettingToggle(false, "TP Exploit"));
    }


    @Override
    public void onDisable() {
        super.onDisable();
        alreadyDone = false;
    }

    @Subscribe
    public void sendPacket(EventSendPacket event) {
        if (event.getPacket() instanceof LoginHelloC2SPacket) {
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
        if (getSettings().get(5).toToggle().state) {
            mc.player.setPosition(targetVec.getX(), targetVec.getY(), targetVec.getZ());
            ModuleManager.getModule(BookCrash.class).setToggled(true);
        } else {
            Vec3d tpDirectionVec = targetVec.subtract(mc.player.getPosVector()).normalize();
            int chunkX = (int) Math.floor(mc.player.getPosVector().x / 16.0D);
            int chunkZ = (int) Math.floor(mc.player.getPosVector().z / 16.0D);
            if (mc.world.isChunkLoaded(chunkX, chunkZ)) {
                lastPos = mc.player.getPosVector();
                if (targetVec.distanceTo(mc.player.getPosVector()) < 0.3D || getSettings().get(3).toSlider().getValue() == 0 || mc.player.deathTime == 0) {
                    alreadyDone = false;
                    getSettings().get(0).toSlider().setValue(0.0d);
                    getSettings().get(1).toSlider().setValue(0.0d);
                    getSettings().get(2).toSlider().setValue(0.0d);
                    getSettings().get(3).toSlider().setValue(1.0d);
                    BleachHack.eventBus.unregister(this);
                }
                if (targetVec.distanceTo(mc.player.getPosVector()) > getSettings().get(3).toSlider().getValue()) {
                    getSettings().get(3).toSlider().setValue(targetVec.distanceTo(mc.player.getPosVector()));
                }

                Vec3d vec;
                double dirX;
                double dirY;
                double dirZ;
                if (targetVec.distanceTo(mc.player.getPosVector()) >= getSettings().get(3).toSlider().getValue()) {
                    dirX = targetVec.getX() / mc.player.getPosVector().getX();
                    dirY = targetVec.getY() / mc.player.getPosVector().getY();
                    dirZ = targetVec.getZ() / mc.player.getPosVector().getZ();
                    vec = tpDirectionVec.multiply(getSettings().get(3).toSlider().getValue());
                /*if (mc.player.getPosVector().getX() - 0.3D < targetVec.getX() || targetVec.getX() < mc.player.getPosVector().getX() + 0.3D) {
                    if (Math.signum(targetVec.getX() - mc.player.getPosVector().getX()) == 1.0D) {
                        toX = getSettings().get(3).toSlider().getValue()/3;
                        remainder = getSettings().get(3).toSlider().getValue()/3 - toX;
                    } else if (Math.signum(targetVec.getX() - mc.player.getPosVector().getX()) == -1.0D) toX = getSettings().get(3).toSlider().getValue()/-3;
                }
                if (mc.player.getPosVector().getY() - 0.3D < targetVec.getY() || targetVec.getY() < mc.player.getPosVector().getY() + 0.3D) {
                    if (Math.signum(targetVec.getY() - mc.player.getPosVector().getY()) == 1.0D) {
                        toY = getSettings().get(3).toSlider().getValue()/3 + remainder;
                        remainder = getSettings().get(3).toSlider().getValue()/3 - toX;
                    } else if (Math.signum(targetVec.getY() - mc.player.getPosVector().getY()) == -1.0D) toY = getSettings().get(3).toSlider().getValue()/-3 - remainder;
                }
                if (mc.player.getPosVector().getZ() - 0.3D < targetVec.getZ() || targetVec.getX() < mc.player.getPosVector().getZ() + 0.3D) {
                    if (Math.signum(targetVec.getZ() - mc.player.getPosVector().getZ()) == 1.0D) {
                        toZ = getSettings().get(3).toSlider().getValue()/3 + remainder;
                    } else if (Math.signum(targetVec.getZ() - mc.player.getPosVector().getZ()) == -1.0D) toZ = getSettings().get(3).toSlider().getValue()/-3 - remainder;
                }
                remainder = 0.0D;
                mc.player.setPosition(mc.player.getPos().getX() + toX, mc.player.getPos().getY() + toY, mc.player.getPos().getZ() + toZ);*/
                    mc.player.setPosition(mc.player.getPos().getX() + vec.getX(), mc.player.getPos().getY() + vec.getY(), mc.player.getPos().getZ() + vec.getZ());
                } else {
                    mc.player.setPosition(targetVec.getX(), targetVec.getY(), targetVec.getZ());
                }
                lastTp = System.currentTimeMillis();
            } else if (lastTp + 2000L > System.currentTimeMillis()) {
                mc.player.setPosition(lastPos.x, lastPos.y, lastPos.z);
            }
        }

    }

}
