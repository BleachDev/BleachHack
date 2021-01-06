package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.file.BleachFileMang;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class CmdItemList extends Command {

    @Override
    public String getAlias() {
        return "itemlist";
    }

    @Override
    public String getDescription() {
        return "Makes a txt file with all item IDs";
    }

    @Override
    public String getSyntax() {
        return "itemlist";
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
