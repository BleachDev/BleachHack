/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.operation;

import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.QuadColor;

import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class RemoveOperation extends Operation {

	protected RemoveOperation(BlockPos pos) {
		this.pos = pos;
	}

	public static OperationBlueprint blueprint(int localX, int localY, int localZ) {
		return (origin, dir) -> new RemoveOperation(origin.add(rotate(localX, localY, localZ, dir)));
	}

	@Override
	public boolean canExecute() {
		if (mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos)) < 4.5) {
			for (Direction d: Direction.values()) {
				if (!mc.world.getBlockState(pos.offset(d)).isSideSolidFullSquare(mc.world, pos.offset(d), d.getOpposite())) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean execute() {
		for (Direction d: Direction.values()) {
			if (!mc.world.getBlockState(pos.offset(d)).isSideSolidFullSquare(mc.world, pos.offset(d), d.getOpposite())) {
				mc.interactionManager.updateBlockBreakingProgress(pos, d);
				mc.player.swingHand(Hand.MAIN_HAND);

				return mc.world.getBlockState(pos).isAir();
			}
		}

		return false;
	}

	@Override
	public boolean verify() {
		return mc.world.getBlockState(pos).isAir();
	}

	@Override
	public void render() {
		Renderer.drawBoxBoth(pos, QuadColor.single(1f, 0f, 0f, 0.3f), 2.5f);
	}

}
