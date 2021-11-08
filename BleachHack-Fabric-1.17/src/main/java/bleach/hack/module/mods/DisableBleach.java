/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import bleach.hack.module.Module;
import com.google.gson.JsonPrimitive;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.ModuleManager;
import bleach.hack.gui.BleachTitleScreen;
import bleach.hack.util.io.BleachFileHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import bleach.hack.gui.window.WindowManagerScreen;

public class DisableBleach extends Module {
    public DisableBleach() {
        super("DisableBleach", KEY_UNBOUND, ModuleCategory.MISC, "Unloads BleachHack. IRREVERSIBLE!");
    }

    @Override
    public void onEnable(boolean inWorld) {
        ShouldSaveSettings = false;
        BleachTitleScreen.unload = true;
        ClickGui.clickGui.clearWindows();
        ModuleManager.unload = true;
        for (Module m : ModuleManager.getModules()) {
            m.ShouldSaveSettings = false;
            m.setEnabled(false);
        }
        super.onEnable(inWorld);
    }
}
