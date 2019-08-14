package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.event.events.EventTick;
import com.google.common.eventbus.Subscribe;
import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;
import net.minecraft.server.network.packet.TeleportConfirmC2SPacket;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class PacketFly extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode(new String[] {"Phase", "Packet"},  "Mode: "),
			new SettingSlider(0.05, 2, 0.5, 2, "HSpeed: "),
			new SettingSlider(0.05, 2, 0.5, 2, "VSpeed: "),
			new SettingSlider(0, 40, 20, 0, "Fall: "));
	
	public PacketFly() {
		super("PacketFly", GLFW.GLFW_KEY_H, Category.MOVEMENT, "Allows you to fly with packets.", settings);
	}
	
	private double posX, posY, posZ;
	private int timer = 0;

	@Override
	public void onEnable() {
		super.onEnable();
		posX = mc.player.x;
		posY = mc.player.y;
		posZ = mc.player.z;
	}

	@Subscribe
	public void onTick(EventTick eventTick) {
		double hspeed = getSettings().get(1).toSlider().getValue();
		double vspeed = getSettings().get(2).toSlider().getValue();
		
		if(!mc.player.isAlive()) return;
		timer++;
		mc.player.setVelocity(0, 0, 0);
		
		if(getSettings().get(0).toMode().mode == 0) {
			if(mc.options.keyJump.isPressed()) posY += vspeed;
			if(mc.options.keySneak.isPressed()) posY -= vspeed;
			
			Vec3d forward = new Vec3d(0,0,hspeed).rotateY(-(float) Math.toRadians(mc.player.yaw));
			Vec3d strafe = forward.rotateY((float) Math.toRadians(90));
			if(mc.options.keyForward.isPressed()) { posX += forward.x; posZ += forward.z; }
			if(mc.options.keyBack.isPressed()) { posX -= forward.x; posZ -= forward.z; }
			if(mc.options.keyLeft.isPressed()) { posX += strafe.x; posZ += strafe.z; }
			if(mc.options.keyRight.isPressed()) { posX -= strafe.x; posZ -= strafe.z; }
			
			if(timer > getSettings().get(3).toSlider().getValue()) {
				posY -= 0.2;
				timer = 0;
			}
			
			mc.player.noClip = true;
			mc.player.setPosition(posX, posY, posZ);
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket());
			mc.player.networkHandler.sendPacket(new TeleportConfirmC2SPacket());
			
		}else if(getSettings().get(0).toMode().mode == 1) {
			double mX = 0; double mY = 0; double mZ = 0;
			if(mc.player.headYaw != mc.player.yaw) {
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(
						mc.player.headYaw, mc.player.pitch, mc.player.onGround));
				return;
			}
			
			if(mc.options.keyJump.isPressed()) mY = 0.062;
			if(mc.options.keySneak.isPressed()) mY = -0.062;
			
			if(mc.options.keyForward.isPressed()) {
				if(mc.player.getMovementDirection().equals(Direction.NORTH)) mZ = -0.275;
				if(mc.player.getMovementDirection().equals(Direction.EAST)) mX = 0.275;
				if(mc.player.getMovementDirection().equals(Direction.SOUTH)) mZ = 0.275;
				if(mc.player.getMovementDirection().equals(Direction.WEST)) mX = -0.275;
			}
			
			if(timer > getSettings().get(3).toSlider().getValue()) {
				mX = 0;
				mZ = 0;
				mY = -0.062;
				timer = 0;
			}
			
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(
					mc.player.x + mX, mc.player.y + mY, mc.player.z + mZ, false));
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(
					mc.player.x + mX, mc.player.y - 420.69, mc.player.z + mZ, true));
		}
	}

}
