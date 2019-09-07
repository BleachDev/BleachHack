package bleach.hack.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class ItemContentUtils {

	public static List<ItemStack> getItemsInContainer(ItemStack item) {
		List<ItemStack> items = new ArrayList<>(Collections.nCopies(27, new ItemStack(Items.AIR)));
		CompoundTag nbt = item.getTag();
		
		if(nbt != null && nbt.containsKey("BlockEntityTag")) {
			CompoundTag nbt2 = nbt.getCompound("BlockEntityTag");
			if(nbt2.containsKey("Items")) {
				ListTag nbt3 = (ListTag) nbt2.getTag("Items");
				for(int i = 0; i < nbt3.size(); i++) {
					items.set(nbt3.getCompoundTag(i).getByte("Slot"), ItemStack.fromTag(nbt3.getCompoundTag(i)));
				}
			}
		}
		
		return items;
	}
	
	public static List<String> getTextInBook(ItemStack item) {
		List<String> pages = new ArrayList<>();
		CompoundTag nbt = item.getTag();
		
		if(nbt != null && nbt.containsKey("pages")) {
			ListTag nbt2 = nbt.getList("pages", 8);
			for(int i = 0; i < nbt2.size(); i++) pages.add(nbt2.getString(i));
		}
		
		return pages;
	}
}
