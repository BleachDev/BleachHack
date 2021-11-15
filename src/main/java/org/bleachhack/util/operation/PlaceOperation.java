/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.operation;

import java.util.Random;

import org.bleachhack.util.InventoryUtils;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.WorldRenderUtils;
import org.bleachhack.util.render.color.QuadColor;
import org.bleachhack.util.world.WorldUtils;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class PlaceOperation extends Operation {

	protected Item item;

	public PlaceOperation(BlockPos pos, Item item) {
		this.pos = pos;
		this.item = item;
	}

	@Override
	public boolean canExecute() {
		for (int i = 0; i < 9; i++) {
			if (mc.player.getInventory().getStack(i).getItem() == item) {
				return mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos)) < 4.5;
			}
		}

		return false;
	}

	@Override
	public boolean execute() {
		int slot = InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == item);

		return WorldUtils.placeBlock(pos, slot, 0, false, false, true);
	}

	@Override
	public boolean verify() {
		return true;
	}

	public Item getItem() {
		return item;
	}

	@Override
	public void render() {
		if (getItem() instanceof BlockItem) {
			MatrixStack matrices = WorldRenderUtils.matrixFrom(pos.getX(), pos.getY(), pos.getZ());

			BlockState state = ((BlockItem) getItem()).getBlock().getDefaultState();

			mc.getBlockRenderManager().renderBlock(state, pos, mc.world, matrices,
					mc.getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayers.getMovingBlockLayer(state)),
					false, new Random(0));

			mc.getBufferBuilders().getEntityVertexConsumers().draw(RenderLayers.getMovingBlockLayer(state));

			for (Box box: state.getOutlineShape(mc.world, pos).getBoundingBoxes()) {
				Renderer.drawBoxFill(box.offset(pos), QuadColor.single(0.45f, 0.7f, 1f, 0.4f));
			}
		} else {
			Renderer.drawBoxBoth(pos, QuadColor.single(1f, 1f, 0f, 0.3f), 2.5f);
		}
	}
}
