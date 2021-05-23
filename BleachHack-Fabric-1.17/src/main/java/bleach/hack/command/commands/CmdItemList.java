package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.command.CommandCategory;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.file.BleachFileMang;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class CmdItemList extends Command {

    public CmdItemList() {
        super("itemlist", "Makes a txt file with all item IDs", "itemlist", CommandCategory.MISC,
                "itemids");
    }

    @Override
    public void onCommand(String command, String[] args) {
        BleachFileMang.createFile("itemlist.txt");
        boolean first = true;
        StringBuilder builder = new StringBuilder();
        for (Item item : Registry.ITEM) {
            if (!first) {
                builder.append(",");
                first = false;
            }
            builder.append(Registry.ITEM.getId(item).getPath());
            builder.append('\n');
        }
        BleachFileMang.appendFile(builder.toString(), "itemlist.txt");
        BleachLogger.infoMessage("Saved item list as: itemlist.txt");
    }
}
