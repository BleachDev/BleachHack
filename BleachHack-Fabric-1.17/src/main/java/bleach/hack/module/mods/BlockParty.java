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

import java.util.ArrayList;
import java.util.List;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingToggle;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class BlockParty extends Module {

	private boolean jumping;

	public BlockParty() {
		super("Blockparty", KEY_UNBOUND, Category.MISC, "Wins You Blockparty",
				new SettingToggle("Jump", true).withDesc("Automatically jumps"),
				new SettingToggle("AutoSpeed", true).withDesc("Gives you speed if you are far away from the right block"));
	}

	@Subscribe
	public void onTick(EventTick event) {
		Item item = mc.player.getInventory().getMainHandStack().getItem();
		Block block = Block.getBlockFromItem(item);
		if (block == Blocks.AIR)
			return;

		if (mc.world.getBlockState(mc.player.getBlockPos().add(0, -1, 0)).getBlock() == block
				|| mc.world.getBlockState(mc.player.getBlockPos().add(0, -2, 0)).getBlock() == block) {
			mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
			KeyBinding.setKeyPressed(mc.options.keyForward.getDefaultKey(), false);
			return;
		}

		List<BlockPos> poses = new ArrayList<>();
		for (int x = -50; x < 50; x++) {
			for (int y = -2; y < 1; y++) {
				for (int z = -50; z < 50; z++) {
					if (mc.world.getBlockState(mc.player.getBlockPos().add(x, y, z)).getBlock() == block
							&& mc.world.getBlockState(mc.player.getBlockPos().add(x, y + 1, z)).getBlock() == Blocks.AIR)
						poses.add(mc.player.getBlockPos().add(x, y, z));
				}
			}
		}

		if (poses.isEmpty())
			return;

		poses.sort((a, b) -> Double.compare(a.getSquaredDistance(mc.player.getBlockPos()), b.getSquaredDistance(mc.player.getBlockPos())));

		double diffX = poses.get(0).getX() + 0.5 - mc.player.getX();
		double diffZ = poses.get(0).getZ() + 0.5 - mc.player.getZ();

		float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;

		mc.player.yaw += MathHelper.wrapDegrees(yaw - mc.player.yaw);

		KeyBinding.setKeyPressed(mc.options.keyForward.getDefaultKey(), true);

		if (mc.player.getBlockPos().getSquaredDistance(poses.get(0)) < (mc.player.isSprinting() ? 25 : 8)
				&& Math.abs(mc.player.getVelocity().x) + Math.abs(mc.player.getVelocity().z) > 0.15
				&& mc.player.verticalCollision) {
			mc.player.jump();
			mc.player.verticalCollision = false;
			// mc.player.setPosition(mc.player.getX(), mc.player.y + 0.02,
			// mc.player.getZ());
		}

		if (getSetting(1).asToggle().state && mc.player.fallDistance < 0.25) {
			if (jumping && mc.player.getY() >= mc.player.prevY + 0.399994D) {
				mc.player.setVelocity(mc.player.getVelocity().x, -0.9, mc.player.getVelocity().z);
				mc.player.setPos(mc.player.getX(), mc.player.prevY, mc.player.getZ());
				jumping = false;
			}

			if (mc.player.forwardSpeed != 0.0F && !mc.player.horizontalCollision) {
				if (mc.player.verticalCollision) {
					mc.player.setVelocity(mc.player.getVelocity().x * Math.min(1.3, 0.85 + mc.player.getBlockPos().getSquaredDistance(poses.get(0)) / 300),
							mc.player.getVelocity().y,
							mc.player.getVelocity().z * Math.min(1.3, 0.85 + mc.player.getBlockPos().getSquaredDistance(poses.get(0)) / 300));
					jumping = true;
					mc.player.jump();
				}

				if (jumping && mc.player.getY() >= mc.player.prevY + 0.399994D) {
					mc.player.setVelocity(mc.player.getVelocity().x, -100, mc.player.getVelocity().z);
					jumping = false;
				}
			}
		}
	}
}
