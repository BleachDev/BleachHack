/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

public class ItemContentUtils {

	public static List<ItemStack> getItemsInContainer(ItemStack item) {
		List<ItemStack> items = new ArrayList<>(Collections.nCopies(27, new ItemStack(Items.AIR)));
		NbtCompound nbt = item.getOrCreateNbt().contains("BlockEntityTag", 10)
				? item.getNbt().getCompound("BlockEntityTag") : item.getNbt();

		if (nbt.contains("Items", 9)) {
			NbtList nbt2 = nbt.getList("Items", 10);
			for (int i = 0; i < nbt2.size(); i++) {
				int slot = nbt2.getCompound(i).contains("Slot", 99) ? nbt2.getCompound(i).getByte("Slot") : i;
				items.set(slot, ItemStack.fromNbt(nbt2.getCompound(i)));
			}
		}

		return items;
	}

	public static List<List<String>> getTextInBook(ItemStack item) {
		List<String> pages = new ArrayList<>();
		NbtCompound nbt = item.getNbt();

		if (nbt != null && nbt.contains("pages")) {
			NbtList nbt2 = nbt.getList("pages", 8);
			for (int i = 0; i < nbt2.size(); i++) {
				if (item.getItem() == Items.WRITABLE_BOOK) {
					pages.add(nbt2.getString(i));
				} else {
					Text text = Text.Serializer.fromLenientJson(nbt2.getString(i));

					pages.add(text != null ? text.getString() : nbt2.getString(i));
				}
			}
		}

		List<List<String>> finalPages = new ArrayList<>();

		for (String s : pages) {
			String buffer = "";
			List<String> pageBuffer = new ArrayList<>();

			for (char c : s.toCharArray()) {
				if (MinecraftClient.getInstance().textRenderer.getWidth(buffer) > 114 || buffer.endsWith("\n")) {
					pageBuffer.add(buffer.replace("\n", ""));
					buffer = "";
				}

				buffer += c;
			}

			pageBuffer.add(buffer);
			finalPages.add(pageBuffer);
		}

		return finalPages;
	}
}
