package org.bleachhack.module.mods;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.bleachhack.event.events.EventClientMove;
import org.bleachhack.event.events.EventPacket;
import org.bleachhack.event.events.EventSendMovementPackets;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingSlider;

public class WGBypass extends Module {
    private int timer = 0;

    public WGBypass() {
        super("WGBypass", KEY_UNBOUND, ModuleCategory.MOVEMENT, "Bypasses Worldguard movement restriction.",
                new SettingSlider("Fall", 1, 40, 10, 0).withDesc("Ticks between falls (anticheat)."));
    }

    @Override
    public void onEnable(boolean inWorld) {
        if (!inWorld)
            return;

        super.onEnable(inWorld);
    }

    @BleachSubscribe
    public void onMovementPackets(EventSendMovementPackets event) {
        mc.player.setVelocity(Vec3d.ZERO);
        event.setCancelled(true);
    }

    @BleachSubscribe
    public void onClientMove(EventClientMove event) {
        event.setCancelled(true);
    }

    @BleachSubscribe
    public void onReadPacket(EventPacket.Read event) {
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            PlayerPositionLookS2CPacket p = (PlayerPositionLookS2CPacket) event.getPacket();

            p.yaw = mc.player.getYaw();
            p.pitch = mc.player.getPitch();
        }

    }

    @BleachSubscribe
    public void onSendPacket(EventPacket.Send event) {
        if (event.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround) {
            event.setCancelled(true);
            return;
        }

        if (event.getPacket() instanceof PlayerMoveC2SPacket.Full) {
            event.setCancelled(true);
            PlayerMoveC2SPacket p = (PlayerMoveC2SPacket) event.getPacket();
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(p.getX(0), p.getY(0), p.getZ(0), p.isOnGround()));
        }
    }

    @BleachSubscribe
    public void onTick(EventTick event) {
        if (!mc.player.isAlive())
            return;

        double hspeed = 0.05;
        double vspeed = 0.05;
        timer++;

        Vec3d forward = new Vec3d(0, 0, hspeed).rotateY(-(float) Math.toRadians(Math.round(mc.player.getYaw() / 90) * 90));
        Vec3d moveVec = Vec3d.ZERO;

        if (mc.player.input.pressingForward) {
            if (!mc.player.input.pressingBack){
                moveVec = moveVec.add(forward);
            }
        } else if (mc.player.input.pressingBack){
            moveVec = moveVec.add(forward.negate());
        } else if (mc.player.input.jumping) {
            if (!mc.player.input.sneaking){
                moveVec = moveVec.add(0, vspeed, 0);
            }
        } else if (mc.player.input.sneaking){
            moveVec = moveVec.add(0, -vspeed, 0);
        } else if (mc.player.input.pressingLeft) {
            if (!mc.player.input.pressingRight){
                moveVec = moveVec.add(forward.rotateY((float) Math.toRadians(90)));
            }
        } else if (mc.player.input.pressingRight){
            moveVec = moveVec.add(forward.rotateY((float) -Math.toRadians(90)));
        }

        if (timer > getSetting(0).asSlider().getValue()) {
            moveVec = new Vec3d(0, -vspeed, 0);
            timer = 0;
        }

        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                mc.player.getX() + moveVec.x, mc.player.getY() + moveVec.y, mc.player.getZ() + moveVec.z, false));

        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                mc.player.getX() + moveVec.x, mc.player.getY() - 420.69, mc.player.getZ() + moveVec.z, true));
    }

}
