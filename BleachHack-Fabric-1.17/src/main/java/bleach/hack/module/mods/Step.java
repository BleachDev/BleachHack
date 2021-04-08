/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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

import java.util.ArrayDeque;
import java.util.Deque;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;

public class Step extends Module {

	private boolean flag;
	private int lastStep = 0;
	private Deque<Double> queue = new ArrayDeque<>();

	public Step() {
		super("Step", KEY_UNBOUND, Category.MOVEMENT, "Allows you to Run up blocks like stairs.",
				new SettingMode("Mode", "Packet", "Vanilla", "Spider", "Jump").withDesc("Step mode"),
				new SettingSlider("Height", 0.1, 20, 2, 1).withDesc("How high to be able to step (Vanilla only)"),
				new SettingToggle("Cooldown", false).withDesc("Adds a cooldown between stepping to bypass some anticheats").withChildren(
						new SettingSlider("Amount", 0.01, 1, 0.1, 2).withDesc("How long the cooldown is in seconds")));
	}

	@Override
	public void onDisable() {
		super.onDisable();
		mc.player.stepHeight = 0.5F;
	}

	@Subscribe
	public void onTick(EventTick event) {
		mc.player.stepHeight = getSetting(0).asMode().mode == 1 ? (float) getSetting(1).asSlider().getValue() : 0.5f;
		
		if (!mc.player.horizontalCollision) {
			queue.clear();
		}
		
		if (getSetting(2).asToggle().state) {
			if (!(mc.player.age < lastStep || mc.player.age >= lastStep + getSetting(2).asToggle().getChild(0).asSlider().getValue() * 20)) {
				return;
			}
		}
		
		if (!mc.world.getBlockState(mc.player.getBlockPos().add(0, mc.player.getHeight() + 1, 0)).getMaterial().isReplaceable()
				|| mc.player.input.jumping
				|| !(mc.player.input.pressingForward || mc.player.input.pressingBack || mc.player.input.pressingLeft || mc.player.input.pressingRight)) {
			return;
		}
		
		if (!queue.isEmpty()) {
			mc.player.updatePosition(mc.player.getX(), queue.poll(), mc.player.getZ());
			return;
		}

		if (getSetting(0).asMode().mode == 0 && mc.player.horizontalCollision && mc.player.isOnGround()) {
			if (!isTouchingWall(mc.player.getBoundingBox().offset(0, 1, 0)) || !isTouchingWall(mc.player.getBoundingBox().offset(0, 1.5, 0))) {
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.42, mc.player.getZ(), false));
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.75, mc.player.getZ(), false));
				
				if (isTouchingWall(mc.player.getBoundingBox().offset(0, 1, 0)) && !isTouchingWall(mc.player.getBoundingBox().offset(0, 1.5, 0))) {
					mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1, mc.player.getZ(), false));
					mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.15, mc.player.getZ(), false));
					mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.24, mc.player.getZ(), false));
					mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 1.15, mc.player.getZ(), true));
					mc.player.updatePosition(mc.player.getX(), mc.player.getY() + 1.0, mc.player.getZ());
				} else {
					mc.player.updatePosition(mc.player.getX(), mc.player.getY() + 1, mc.player.getZ());
				}
				
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
				lastStep = mc.player.age;
			}
		} else if (getSetting(0).asMode().mode == 2) {
			if (!mc.player.horizontalCollision && flag) {
				mc.player.setVelocity(mc.player.getVelocity().x, -0.1, mc.player.getVelocity().z);
				lastStep = mc.player.age;
				flag = false;
			} else if (mc.player.horizontalCollision) {
				mc.player.setVelocity(mc.player.getVelocity().x, Math.min((mc.player.getY() + 1) - Math.floor(mc.player.getY()), 0.42), mc.player.getVelocity().z);
				flag = true;
			}
		} else if (getSetting(0).asMode().mode == 3) {
			if (mc.player.horizontalCollision && mc.player.isOnGround()) {
				mc.player.jump();
				flag = true;
			}

			if (flag && !mc.player.horizontalCollision /*pos + 1.065 < mc.player.getY()*/) {
				mc.player.setVelocity(mc.player.getVelocity().x, -0.1, mc.player.getVelocity().z);
				lastStep = mc.player.age;
				flag = false;
			}
		}
	}
	
	private boolean isTouchingWall(Box box) {
		// Check in 2 calls instead of just box.expand(0.01, 0, 0.01) to prevent it getting stuck in corners
		return !mc.world.isSpaceEmpty(box.expand(0.01, 0, 0)) || !mc.world.isSpaceEmpty(box.expand(0, 0, 0.01));
	}
}
