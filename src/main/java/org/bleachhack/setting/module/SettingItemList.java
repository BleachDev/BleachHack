/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.setting.module;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bleachhack.setting.SettingDataHandlers;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;

public class SettingItemList extends SettingList<Item> {

	public SettingItemList(String text, String windowText, Item... defaultItems) {
		this(text, windowText, null, defaultItems);
	}

	public SettingItemList(String text, String windowText, Predicate<Item> filter, Item... defaultItems) {
		super(text, windowText, SettingDataHandlers.ITEM, getAllItems(filter), defaultItems);
	}

	private static Collection<Item> getAllItems(Predicate<Item> filter) {
		return filter == null
				? Registry.ITEM.stream().collect(Collectors.toList())
						: Registry.ITEM.stream().filter(filter).collect(Collectors.toList());
	}

	@Override
	public void renderItem(MinecraftClient mc, MatrixStack matrices, Item item, int x, int y, int w, int h) {
		if (item == null || item == Items.AIR) {
			super.renderItem(mc, matrices, item, x, y, w, h);
		} else {
			RenderSystem.getModelViewStack().push();

			float scale = (h - 2) / 16f;
			float offset = 1f / scale;

			RenderSystem.getModelViewStack().scale(scale, scale, 1f);

			mc.getItemRenderer().renderInGui(new ItemStack(item), (int) ((x + 1) * offset), (int) ((y + 1) * offset));

			RenderSystem.getModelViewStack().pop();
			RenderSystem.applyModelViewMatrix();
		}
	}

	@Override
	public Text getName(Item item) {
		return item.getName();
	}
}
