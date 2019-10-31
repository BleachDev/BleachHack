package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.utils.WorldUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class Flight extends Module {
	
	public Flight() {
		super("Flight", GLFW.GLFW_KEY_G, Category.MOVEMENT, "Allows you to fly",
				new SettingMode("Mode: ", "Normal","Static","Jetpack"),
				new SettingSlider("Speed: ", 0, 5, 1, 1),
				new SettingMode("AntiKick: ", "Off","Fall","Bob","Packet"));
	}

	@Override
	public void onDisable() {
		super.onDisable();
		if(!mc.player.abilities.creativeMode) mc.player.abilities.allowFlying = false;
		mc.player.abilities.flying = false;
	}

	@Subscribe
	public void onTick(EventTick event) {
		float speed = (float) getSettings().get(1).toSlider().getValue();
		
		if(mc.player.age % 20 == 0 && getSettings().get(2).toMode().mode == 3 && !(getSettings().get(0).toMode().mode == 2)) {
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.x, mc.player.y - 0.06, mc.player.z, false));
			mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.x, mc.player.y + 0.06, mc.player.z, true));
		}
		
		if(getSettings().get(0).toMode().mode == 0) {
			mc.player.abilities.setFlySpeed(speed / 10);
			mc.player.abilities.allowFlying = true;
			mc.player.abilities.flying = true;
		}else if(getSettings().get(0).toMode().mode == 1) {
			if(getSettings().get(2).toMode().mode == 0 || getSettings().get(2).toMode().mode == 3) mc.player.setVelocity(0, 0, 0);
			else if(getSettings().get(2).toMode().mode == 1 && WorldUtils.NONSOLID_BLOCKS.contains(mc.world.getBlockState(new BlockPos(mc.player.getPosVector().getX(), mc.player.getPosVector().getY() - 0.069, mc.player.getPosVector().getZ())).getBlock())) mc.player.setVelocity(0, mc.player.age % 20 == 0 ? -0.069 : 0, 0);
			else if(getSettings().get(2).toMode().mode == 2) mc.player.setVelocity(0, mc.player.age % 40 == 0 ? (WorldUtils.NONSOLID_BLOCKS.contains(mc.world.getBlockState(new BlockPos(mc.player.getPosVector().getX(), mc.player.getPosVector().getY() + 1.15, mc.player.getPosVector().getZ())).getBlock()) ? 0.15 : 0) : mc.player.age % 20 == 0 ? (WorldUtils.NONSOLID_BLOCKS.contains(mc.world.getBlockState(new BlockPos(mc.player.getPosVector().getX(), mc.player.getPosVector().getY() - 0.15, mc.player.getPosVector().getZ())).getBlock()) ? -0.15 : 0) : 0, 0);
			Vec3d forward = new Vec3d(0, 0, speed).rotateY(-(float) Math.toRadians(mc.player.yaw));
			Vec3d strafe = forward.rotateY((float) Math.toRadians(90));
			
			if(mc.options.keyJump.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(0, speed, 0));
			if(mc.options.keySneak.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(0, -speed, 0));
			if(mc.options.keyBack.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(-forward.x, 0, -forward.z));
			if(mc.options.keyForward.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(forward.x, 0, forward.z));
			if(mc.options.keyLeft.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(strafe.x, 0, strafe.z));
			if(mc.options.keyRight.isPressed()) mc.player.setVelocity(mc.player.getVelocity().add(-strafe.x, 0, -strafe.z));

		}else if(getSettings().get(0).toMode().mode == 2) {
			if(!mc.options.keyJump.isPressed()) return;
			mc.player.setVelocity(mc.player.getVelocity().x, speed / 3, mc.player.getVelocity().z);
		}
	}
}
