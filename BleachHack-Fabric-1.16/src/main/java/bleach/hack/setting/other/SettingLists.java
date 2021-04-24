/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.setting.other;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.mojang.blaze3d.systems.RenderSystem;

import bleach.hack.setting.base.SettingList;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SettingLists {

	public static SettingList<Block> newBlockList(String text, String windowText, Block... blocks) {
		return newBlockList(text, windowText, null, blocks);
	}

	public static SettingList<Block> newBlockList(String text, String windowText, Predicate<Block> filter,  Block... blocks) {
		List<Block> blockList = filter == null
				? Registry.BLOCK.stream().collect(Collectors.toList())
						: Registry.BLOCK.stream().filter(filter).collect(Collectors.toList());

		return new SettingList<Block>(text, windowText, blockList, blocks) {

			@Override
			public void renderItem(MinecraftClient mc, MatrixStack matrix, Block item, int x, int y, int w, int h) {
				if (item.asItem() == Items.AIR) {
					super.renderItem(mc, matrix, item, x, y, w, h);
				} else {
					RenderSystem.pushMatrix();

					float scale = (h - 2) / 16f;
					float offset = 1f / scale;

					RenderSystem.scalef(scale, scale, 1f);

					DiffuseLighting.enableGuiDepthLighting();
					mc.getItemRenderer().renderInGui(new ItemStack(item.asItem()), (int) ((x + 1) * offset), (int) ((y + 1) * offset));
					DiffuseLighting.disableGuiDepthLighting();

					RenderSystem.popMatrix();
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
		};
	}

	public static SettingList<Item> newItemList(String text, String windowText, Item... items) {
		return newItemList(text, windowText, null, items);
	}

	public static SettingList<Item> newItemList(String text, String windowText, Predicate<Item> filter, Item... items) {
		List<Item> itemList = filter == null
				? Registry.ITEM.stream().collect(Collectors.toList())
						: Registry.ITEM.stream().filter(filter).collect(Collectors.toList());

		return new SettingList<Item>(text, windowText, itemList, items) {

			@Override
			public void renderItem(MinecraftClient mc, MatrixStack matrix, Item item, int x, int y, int w, int h) {
				if (item == Items.AIR) {
					super.renderItem(mc, matrix, item, x, y, w, h);
				} else {
					RenderSystem.pushMatrix();

					float scale = (h - 2) / 16f;
					float offset = 1f / scale;

					RenderSystem.scalef(scale, scale, 1f);

					DiffuseLighting.enableGuiDepthLighting();
					mc.getItemRenderer().renderInGui(new ItemStack(item), (int) ((x + 1) * offset), (int) ((y + 1) * offset));
					DiffuseLighting.disableGuiDepthLighting();

					RenderSystem.popMatrix();
				}
			}

			@Override
			public Item getItemFromString(String string) {
				Item item = Registry.ITEM.get(new Identifier(string));
				return item == Items.AIR ? null : item;
			}

			@Override
			public String getStringFromItem(Item item) {
				return Registry.ITEM.getId(item).toString();
			}
		};
	}
}
