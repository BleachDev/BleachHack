package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.client.CPlayerPacket;

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
	
	public void onEnable() {
		posX = mc.player.posX;
		posY = mc.player.posY;
		posZ = mc.player.posZ;
	}
	
	public void onUpdate() {
		if(this.isToggled()) {
			double hspeed = getSettings().get(1).toSlider().getValue();
			double vspeed = getSettings().get(2).toSlider().getValue();
			
			if(!mc.player.isAlive()) return;
			timer++;
			mc.player.setVelocity(0, 0, 0);
			mc.player.setMotion(0, 0, 0);
			
			if(getSettings().get(0).toMode().mode == 0) {
				if(mc.gameSettings.keyBindJump.isKeyDown()) posY += vspeed;
				if(mc.gameSettings.keyBindSneak.isKeyDown()) posY -= vspeed;
				
				if(mc.gameSettings.keyBindForward.isKeyDown()) {
					if(EntityUtils.getDirectionFacing(mc.player) == 0) posZ -= hspeed;
					if(EntityUtils.getDirectionFacing(mc.player) == 1) posX += hspeed;
					if(EntityUtils.getDirectionFacing(mc.player) == 2) posZ += hspeed;
					if(EntityUtils.getDirectionFacing(mc.player) == 3) posX -= hspeed;
				}
				
				if(mc.gameSettings.keyBindBack.isKeyDown()) {
					if(EntityUtils.getDirectionFacing(mc.player) == 0) posZ += hspeed;
					if(EntityUtils.getDirectionFacing(mc.player) == 1) posX -= hspeed;
					if(EntityUtils.getDirectionFacing(mc.player) == 2) posZ -= hspeed;
					if(EntityUtils.getDirectionFacing(mc.player) == 3) posX += hspeed;
				}
				
				if(timer > getSettings().get(3).toSlider().getValue()) {
					posY -= 0.2;
					timer = 0;
				}
				
				mc.player.noClip = true;
				mc.player.setPosition(posX, posY, posZ);
				mc.player.connection.sendPacket(new CPlayerPacket());
				mc.player.connection.sendPacket(new CConfirmTeleportPacket());
				
			}else if(getSettings().get(0).toMode().mode == 1) {
				double mX = 0; double mY = 0; double mZ = 0;
				if(mc.player.rotationYawHead != mc.player.rotationYaw) {
					mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(
							mc.player.rotationYawHead, mc.player.rotationPitch, mc.player.onGround));
					return;
				}
				
				if(mc.gameSettings.keyBindJump.isKeyDown()) mY = 0.062;
				if(mc.gameSettings.keyBindSneak.isKeyDown()) mY = -0.062;
				
				if(mc.gameSettings.keyBindForward.isKeyDown()) {
					if(EntityUtils.getDirectionFacing(mc.player) == 0) mZ = -0.275;
					if(EntityUtils.getDirectionFacing(mc.player) == 1) mX = 0.275;
					if(EntityUtils.getDirectionFacing(mc.player) == 2) mZ = 0.275;
					if(EntityUtils.getDirectionFacing(mc.player) == 3) mX = -0.275;
				}
				
				if(timer > getSettings().get(3).toSlider().getValue()) {
					mX = 0;
					mZ = 0;
					mY = -0.062;
					timer = 0;
				}
				
				mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(
						mc.player.posX + mX, mc.player.posY + mY, mc.player.posZ + mZ, false));
				mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(
						mc.player.posX + mX, mc.player.posY - 420.69, mc.player.posZ + mZ, true));
			}
		}
	}

}
