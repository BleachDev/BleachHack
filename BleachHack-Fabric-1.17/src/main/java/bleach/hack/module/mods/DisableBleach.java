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
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.ModuleManager;

public class DisableBleach extends Module {
    public DisableBleach() {
        super("DisableBleach", KEY_UNBOUND, ModuleCategory.MISC, "Unloads BleachHack. IRREVERTABLE!");
    }

    @Override
    public void onEnable(boolean inWorld) {
        ShouldSaveSettings = false;
        for (Module m : ModuleManager.getModules()) {
            m.ShouldSaveSettings = false;
            m.setEnabled(false);
            ModuleManager.unloadModule(m);
        }
        ClickGui.clickGui.clearWindows();
        this.toggle();
        super.onEnable(inWorld);
    }
}
