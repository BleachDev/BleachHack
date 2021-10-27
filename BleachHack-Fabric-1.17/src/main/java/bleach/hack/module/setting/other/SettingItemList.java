/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.setting.other;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.mojang.blaze3d.systems.RenderSystem;

import bleach.hack.module.setting.base.SettingList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SettingItemList extends SettingList<Item> {

	public SettingItemList(String text, String windowText, Item... items) {
		this(text, windowText, null, items);
	}

	public SettingItemList(String text, String windowText, Predicate<Item> filter, Item... items) {
		super(text, windowText, getAllItems(filter), items);
	}

	private static Collection<Item> getAllItems(Predicate<Item> filter) {
		return filter == null
				? Registry.ITEM.stream().collect(Collectors.toList())
						: Registry.ITEM.stream().filter(filter).collect(Collectors.toList());
	}

	@Override
	public void renderItem(MinecraftClient mc, MatrixStack matrices, Item item, int x, int y, int w, int h) {
		if (item == Items.AIR) {
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
	public Item getItemFromString(String string) {
		Item item = Registry.ITEM.get(new Identifier(string));
		return item == Items.AIR ? null : item;
	}

	@Override
	public String getStringFromItem(Item item) {
		return Registry.ITEM.getId(item).toString();
	}
}
