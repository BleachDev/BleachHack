/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.setting.other;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bleachhack.module.setting.base.SettingList;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SettingBlockList extends SettingList<Block> {

	public SettingBlockList(String text, String windowText, Block... blocks) {
		this(text, windowText, null, blocks);
	}

	public SettingBlockList(String text, String windowText, Predicate<Block> filter, Block... blocks) {
		super(text, windowText, getAllBlocks(filter), blocks);
	}

	private static Collection<Block> getAllBlocks(Predicate<Block> filter) {
		return filter == null
				? Registry.BLOCK.stream().collect(Collectors.toList())
						: Registry.BLOCK.stream().filter(filter).collect(Collectors.toList());
	}

	@Override
	public void renderItem(MinecraftClient mc, MatrixStack matrices, Block item, int x, int y, int w, int h) {
		if (item.asItem() == Items.AIR) {
			super.renderItem(mc, matrices, item, x, y, w, h);
		} else {
			RenderSystem.getModelViewStack().push();

			float scale = (h - 2) / 16f;
			float offset = 1f / scale;

			RenderSystem.getModelViewStack().scale(scale, scale, 1f);

			mc.getItemRenderer().renderInGui(new ItemStack(item.asItem()), (int) ((x + 1) * offset), (int) ((y + 1) * offset));

			RenderSystem.getModelViewStack().pop();
			RenderSystem.applyModelViewMatrix();
		}
	}

	@Override
	public Block getItemFromString(String string) {
		Block bl = Registry.BLOCK.get(new Identifier(string));
		return bl == Blocks.AIR ? null : bl;
	}

	@Override
	public String getStringFromItem(Block item) {
		return Registry.BLOCK.getId(item).toString();
	}
}
