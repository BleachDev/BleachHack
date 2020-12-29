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

import org.lwjgl.glfw.GLFW;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;

public class SpeedHack extends Module {

	private boolean jumping;

	public SpeedHack() {
		super("SpeedHack", GLFW.GLFW_KEY_V, Category.MOVEMENT, "Allows you to go faster, what did you expect?",
				new SettingMode("Mode", "OnGround", "MiniHop", "Bhop"),
				new SettingSlider("Speed", 0.1, 10, 2, 1));
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (mc.options.keySneak.isPressed())
			return;
		double speeds = getSetting(1).asSlider().getValue() / 30;

		/* OnGround */
		if (getSetting(0).asMode().mode == 0) {
			if (mc.options.keyJump.isPressed() || mc.player.fallDistance > 0.25)
				return;

			if (jumping && mc.player.getY() >= mc.player.prevY + 0.399994D) {
				mc.player.setVelocity(mc.player.getVelocity().x, -0.9, mc.player.getVelocity().z);
				mc.player.setPos(mc.player.getX(), mc.player.prevY, mc.player.getZ());
				jumping = false;
			}

			if (mc.player.forwardSpeed != 0.0F && !mc.player.horizontalCollision) {
				if (mc.player.verticalCollision) {
					mc.player.setVelocity(mc.player.getVelocity().x * (0.85 + speeds), mc.player.getVelocity().y, mc.player.getVelocity().z * (0.85 + speeds));
					jumping = true;
					mc.player.jump();
					// 1.0379
				}

				if (jumping && mc.player.getY() >= mc.player.prevY + 0.399994D) {
					mc.player.setVelocity(mc.player.getVelocity().x, -100, mc.player.getVelocity().z);
					jumping = false;
				}

			}

			/* MiniHop */
		} else if (getSetting(0).asMode().mode == 1) {
			if (mc.player.horizontalCollision || mc.options.keyJump.isPressed() || mc.player.forwardSpeed == 0)
				return;
			if (mc.player.isOnGround())
				mc.player.jump();
			else if (mc.player.getVelocity().y > 0) {
				mc.player.setVelocity(mc.player.getVelocity().x * (0.9 + speeds), -1, mc.player.getVelocity().z * (0.9 + speeds));
				mc.player.input.movementSideways += 1.5F;
			}

			/* Bhop */
		} else if (getSetting(0).asMode().mode == 2) {
			if (mc.player.forwardSpeed > 0 && mc.player.isOnGround()) {
				mc.player.jump();
				mc.player.setVelocity(mc.player.getVelocity().x * (0.65 + speeds), 0.255556, mc.player.getVelocity().z * (0.65 + speeds));
				mc.player.sidewaysSpeed += 3.0F;
				mc.player.jump();
				mc.player.setSprinting(true);
			}
		}
	}
}
