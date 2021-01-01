/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
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

import java.io.IOException;
import java.io.FileWriter;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.file.BleachFileMang;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class ItemList extends Module {


    public ItemList() {
        super("ItemList", KEY_UNBOUND, Category.DEV, "Makes a txt file with all item IDs");
    }

    @Override
    public void onEnable() {
        super.onEnable();
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

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
