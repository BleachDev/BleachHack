package org.bleachhack.module.mods;

import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;

public class IamBot extends Module {
    public IamBot() {
        super("IamBot", KEY_UNBOUND, ModuleCategory.MOVEMENT, "Makes you move like a bot.",
                new SettingMode("Mode", "100x", "1000x").withDesc("Switches between two movement methods"));
    }
}
