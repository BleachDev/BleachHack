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

import bleach.hack.event.events.EventClientMove;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.WorldUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.c2s.play.ClickWindowC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class NoSlow extends Module {

	private Vec3d addVelocity = Vec3d.ZERO;

	public NoSlow() {
		super("NoSlow", KEY_UNBOUND, Category.MOVEMENT, "Disables Stuff From Slowing You Down",
				new SettingToggle("Slowness", true),
				new SettingToggle("Soul Sand", true),
				new SettingToggle("Slime Blocks", true),
				new SettingToggle("Webs", true),
				new SettingToggle("Berry Bush", true),
				new SettingToggle("Items", true),
				new SettingToggle("Inventory", true).withDesc("Allows you to move in inventories").withChildren(
						new SettingToggle("Sneaking", false).withDesc("Enabled the sneak key while in a inventory"),
						new SettingToggle("NCP Bypass", false).withDesc("Allows you to move items around while running on NCP"),
						new SettingToggle("Rotate", true).withDesc("Allows you to use the arrow keys to rotate").withChildren(
								new SettingToggle("Limit Pitch", true).withDesc("Prevents you from being able to do a 360 pitch spin"),
								new SettingToggle("Anti-Spinbot", true).withDesc("Adds a random amount of rotation when spinning to prevent spinbot detects"))));
	}

	@Subscribe
	public void onClientMove(EventClientMove event) {
		if (!isToggled())
			return;

		/* Slowness */
		if (getSetting(0).asToggle().state && (mc.player.getStatusEffect(StatusEffects.SLOWNESS) != null || mc.player.getStatusEffect(StatusEffects.BLINDNESS) != null)) {
			if (mc.options.keyForward.isPressed()
					&& mc.player.getVelocity().x > -0.15 && mc.player.getVelocity().x < 0.15
					&& mc.player.getVelocity().z > -0.15 && mc.player.getVelocity().z < 0.15) {
				mc.player.setVelocity(mc.player.getVelocity().add(addVelocity));
				addVelocity = addVelocity.add(new Vec3d(0, 0, 0.05).rotateY(-(float) Math.toRadians(mc.player.yaw)));
			} else
				addVelocity = addVelocity.multiply(0.75, 0.75, 0.75);
		}

		/* Soul Sand */
		if (getSetting(1).asToggle().state && mc.world.getBlockState(mc.player.getBlockPos()).getBlock() == Blocks.SOUL_SAND) {
			mc.player.setVelocity(mc.player.getVelocity().multiply(2.5, 1, 2.5));
		}

		/* Slime Block */
		if (getSetting(2).asToggle().state
				&& mc.world.getBlockState(new BlockPos(mc.player.getPos().add(0, -0.01, 0))).getBlock() == Blocks.SLIME_BLOCK && mc.player.isOnGround()) {
			double d = Math.abs(mc.player.getVelocity().y);
			if (d < 0.1D && !mc.player.bypassesSteppingEffects()) {
				double e = 1 / (0.4D + d * 0.2D);
				mc.player.setVelocity(mc.player.getVelocity().multiply(e, 1.0D, e));
			}
		}

		/* Web */
		if (getSetting(3).asToggle().state && WorldUtils.doesBoxTouchBlock(mc.player.getBoundingBox(), Blocks.COBWEB)) {
			// still kinda scuffed until i get an actual mixin
			mc.player.slowMovement(mc.player.getBlockState(), new Vec3d(1.75, 1.75, 1.75));
		}

		/* Berry Bush */
		if (getSetting(4).asToggle().state && WorldUtils.doesBoxTouchBlock(mc.player.getBoundingBox(), Blocks.SWEET_BERRY_BUSH)) {
			// also scuffed
			mc.player.slowMovement(mc.player.getBlockState(), new Vec3d(1.7, 1.7, 1.7));
		}

		// Items handled in MixinPlayerEntity:sendMovementPackets_isUsingItem
	}

	@Subscribe
	public void onTick(EventTick event) {
		/* Inventory */
		if (getSetting(6).asToggle().state && mc.currentScreen != null && !(mc.currentScreen instanceof ChatScreen)) {
			for (KeyBinding k : new KeyBinding[] { mc.options.keyForward, mc.options.keyBack,
					mc.options.keyLeft, mc.options.keyRight, mc.options.keyJump, mc.options.keySprint }) {
				k.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(),
						InputUtil.fromTranslationKey(k.getBoundKeyTranslationKey()).getCode()));
			}

			if (getSetting(6).asToggle().asToggle().getChild(0).asToggle().state) {
				mc.options.keySneak.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(),
						InputUtil.fromTranslationKey(mc.options.keySneak.getBoundKeyTranslationKey()).getCode()));
			}

			if (getSetting(6).asToggle().asToggle().getChild(2).asToggle().state) {
				float yaw = 0f;
				float pitch = 0f;

				if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT))
					yaw -= 4f;
				if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT))
					yaw += 4f;
				if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_UP))
					pitch -= 4f;
				if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_DOWN))
					pitch += 4f;

				if (getSetting(6).asToggle().asToggle().getChild(2).asToggle().asToggle().getChild(1).asToggle().state) {
					if (yaw == 0f && pitch != 0f)
						yaw += -0.1 + Math.random() / 5f;
					else
						yaw *= 0.75f + Math.random() / 2f;

					if (pitch == 0f && yaw != 0f)
						pitch += -0.1 + Math.random() / 5f;
					else
						pitch *= 0.75f + Math.random() / 2f;
				}

				mc.player.yaw += yaw;

				if (getSetting(6).asToggle().asToggle().getChild(2).asToggle().asToggle().getChild(0).asToggle().state) {
					mc.player.pitch = MathHelper.clamp(mc.player.pitch + pitch, -90f, 90f);
				} else {
					mc.player.pitch += pitch;
				}
			}
		}
	}

	@Subscribe
	public void onSendPacket(EventSendPacket event) {
		if (event.getPacket() instanceof ClickWindowC2SPacket && getSetting(6).asToggle().asToggle().getChild(1).asToggle().state) {
			mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING));
		}
	}
}
