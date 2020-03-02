package bleach.hack.command.commands;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import bleach.hack.command.Command;
import bleach.hack.utils.BleachLogger;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.util.Hand;

public class CmdRbook extends Command {

	@Override
	public String getAlias() {
		return "rbook";
	}

	@Override
	public String getDescription() {
		return "Generates a random book";
	}

	@Override
	public String getSyntax() {
		return "rbook [pages] [start char] [end char] [chrs/page]";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		ItemStack item = mc.player.inventory.getMainHandStack();
		
		if(item.getItem() != Items.WRITABLE_BOOK) {
			BleachLogger.errorMessage("Not Holding A Writable Book!");
			return;
		}
		
		int pages = 100; 
		int startChar = 0x0;
		int endChar = 0x10FFFF;
		int pageChars = 210;
		try { pages = Math.min(Integer.parseInt(args[0]), 100); } catch(Exception e) {}
		try { startChar = Integer.parseInt(args[1]); } catch(Exception e) {}
		try { endChar = Integer.parseInt(args[2]); } catch(Exception e) {}
		try { pageChars = Integer.parseInt(args[3]); } catch(Exception e) {}
		
		IntStream chars = new Random().ints(startChar, endChar + 1);
		String text = chars.limit(pageChars*100).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());
		
		ListTag textSplit = new ListTag();
		
		for (int t = 0; t < pages; t++) textSplit.add(StringTag.of(text.substring(t * pageChars, (t + 1) * pageChars)));
		
		item.getOrCreateTag().put("pages", textSplit);
		mc.player.networkHandler.sendPacket(new BookUpdateC2SPacket(item, false, Hand.MAIN_HAND));
	}

}
