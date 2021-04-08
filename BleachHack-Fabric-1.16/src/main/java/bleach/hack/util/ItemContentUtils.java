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
package bleach.hack.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.Text;

public class ItemContentUtils {

	public static List<ItemStack> getItemsInContainer(ItemStack item) {
		List<ItemStack> items = new ArrayList<>(Collections.nCopies(27, new ItemStack(Items.AIR)));
		CompoundTag nbt = item.getTag();

		if (nbt != null && nbt.contains("BlockEntityTag")) {
			CompoundTag nbt2 = nbt.getCompound("BlockEntityTag");
			if (nbt2.contains("Items")) {
				ListTag nbt3 = (ListTag) nbt2.get("Items");
				for (int i = 0; i < nbt3.size(); i++) {
					items.set(nbt3.getCompound(i).getByte("Slot"), ItemStack.fromTag(nbt3.getCompound(i)));
				}
			}
		}

		return items;
	}

	public static List<List<String>> getTextInBook(ItemStack item) {
		List<String> pages = new ArrayList<>();
		CompoundTag nbt = item.getTag();

		if (nbt != null && nbt.contains("pages")) {
			ListTag nbt2 = nbt.getList("pages", 8);
			for (int i = 0; i < nbt2.size(); i++) {
				Text text = Text.Serializer.fromLenientJson(nbt2.getString(i));

				pages.add(text != null ? text.getString() : nbt2.getString(i));
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
