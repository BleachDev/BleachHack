/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util.operation;

import bleach.hack.util.world.WorldUtils;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * THIS DOES NOT PLACE A BLOCK ON A SPECIFIC SIDE OF A BLOCK!!
 * This faces you in a direction then places a block, useful for directional blocks like pistons
 */
public class PlaceDirOperation extends PlaceOperation {

	private Direction dir;
	private boolean faced = false;

	public PlaceDirOperation(BlockPos pos, Item item, Direction dir) {
		super(pos, item);
		this.dir = dir;
	}

	@Override
	public boolean execute() {
		for (int i = 0; i < 9; i++) {
			if (mc.player.getInventory().getStack(i).getItem() == item) {
				if (WorldUtils.canPlaceBlock(pos)) {
					Vec3d lookPos = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0).add(dir.getOffsetX(), dir.getOffsetY(), dir.getOffsetZ());
					WorldUtils.facePosPacket(lookPos.getX(), lookPos.getY(), lookPos.getZ());

					if (!faced) {
						faced = true;
						return false;
					}

					return WorldUtils.placeBlock(pos, i, 0, false, false, true);
				}
			}
		}

		return false;
	}

	@Override
	public boolean verify() {
		return true;
	}
}
