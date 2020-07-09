/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
 * Copyright (c) 2019 Bleach.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.module.mods;

import bleach.hack.event.events.EventClientMove;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;

import com.google.common.eventbus.Subscribe;
import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.PlayerCopyEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.packet.ClientCommandC2SPacket;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;

public class Freecam extends Module {
	
	private PlayerCopyEntity dummy;
	private double[] playerPos;
	private float[] playerRot;
	private Entity riding;
	
	public Freecam() {
		super("Freecam", GLFW.GLFW_KEY_U, Category.PLAYER, "Its freecam, you know what it does",
				new SettingSlider("Speed: ", 0, 2, 0.5, 2));
	}

	@Override
	public void onEnable() {
		playerPos = new double[] {mc.player.x, mc.player.y, mc.player.z};
		playerRot = new float[] {mc.player.yaw, mc.player.pitch};

		dummy = new PlayerCopyEntity();
		dummy.copyPositionAndRotation(mc.player);
		dummy.setBoundingBox(dummy.getBoundingBox().expand(0.1));

		dummy.spawn();
		
		if (mc.player.getVehicle() != null) {
			riding = mc.player.getVehicle();
			mc.player.getVehicle().removeAllPassengers();
		}
		
		super.onEnable();
	}

	@Override
	public void onDisable() {
		dummy.despawn();
		mc.player.noClip = false;
		
		mc.player.setPositionAndAngles(playerPos[0], playerPos[1], playerPos[2], playerRot[0], playerRot[1]);
		mc.player.setVelocity(0, 0, 0);
		
		if (riding != null && mc.world.getEntityById(riding.getEntityId()) != null) {
			mc.player.startRiding(riding);
		}
		
		super.onDisable();
	}

	@Subscribe
    public void sendPacket(EventSendPacket event) {
        if (event.getPacket() instanceof ClientCommandC2SPacket || event.getPacket() instanceof PlayerMoveC2SPacket) {
        	event.setCancelled(true);
        }
    }
	
	@Subscribe
	public void onClientMove(EventClientMove event) {
		mc.player.noClip = true;
	}
	
	@Subscribe
	public void onTick(EventTick event) {
		mc.player.setSprinting(true);
		mc.player.setVelocity(0, 0, 0);
		mc.player.onGround = false;
		mc.player.abilities.setFlySpeed((float) getSettings().get(0).toSlider().getValue());
		
		if(mc.options.keyJump.isPressed()) mc.player.addVelocity(0, getSettings().get(0).toSlider().getValue() / 4, 0);
		if(mc.options.keySneak.isPressed()) mc.player.addVelocity(0, -getSettings().get(0).toSlider().getValue() / 4, 0);
	}

}
