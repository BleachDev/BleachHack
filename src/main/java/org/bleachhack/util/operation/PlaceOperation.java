/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.operation;

import org.apache.commons.lang3.ArrayUtils;
import org.bleachhack.util.InventoryUtils;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.WorldRenderer;
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
import net.minecraft.util.math.random.Random;

public class PlaceOperation extends Operation {

	protected Item[] items;

	protected PlaceOperation(BlockPos pos, Item... items) {
		this.pos = pos;
		this.items = items;
	}

	public static OperationBlueprint blueprint(int localX, int localY, int localZ, Item... items) {
		return (origin, dir) -> new PlaceOperation(origin.add(rotate(localX, localY, localZ, dir)), items);
	}

	@Override
	public boolean canExecute() {
		if (mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos)) > 4.5)
			return false;

		return InventoryUtils.getSlot(true, i -> ArrayUtils.contains(items, mc.player.getInventory().getStack(i).getItem())) != -1;
	}

	@Override
	public boolean execute() {
		int slot = InventoryUtils.getSlot(true, i -> ArrayUtils.contains(items, mc.player.getInventory().getStack(i).getItem()));

		return WorldUtils.placeBlock(pos, slot, 0, false, false, true);
	}

	@Override
	public boolean verify() {
		return true;
	}

	public Item[] getItems() {
		return items;
	}

	@Override
	public void render() {
		Item item = getItems()[0];
		if (item instanceof BlockItem) {
			MatrixStack matrices = WorldRenderer.matrixFrom(pos.getX(), pos.getY(), pos.getZ());

			BlockState state = ((BlockItem) item).getBlock().getDefaultState();

			mc.getBlockRenderManager().renderBlock(state, pos, mc.world, matrices,
					mc.getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayers.getMovingBlockLayer(state)),
					false, Random.create(0L));

			mc.getBufferBuilders().getEntityVertexConsumers().draw(RenderLayers.getMovingBlockLayer(state));

			for (Box box: state.getOutlineShape(mc.world, pos).getBoundingBoxes()) {
				Renderer.drawBoxFill(box.offset(pos), QuadColor.single(0.45f, 0.7f, 1f, 0.4f));
			}
		} else {
			Renderer.drawBoxBoth(pos, QuadColor.single(1f, 1f, 0f, 0.3f), 2.5f);
		}
	}
}
