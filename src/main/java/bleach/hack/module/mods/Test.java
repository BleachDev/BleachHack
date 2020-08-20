package bleach.hack.module.mods;

import bleach.hack.module.Category;
import bleach.hack.module.Module;

public class Test extends Module {

    public Test() {
        super("Test", KEY_UNBOUND, Category.WORLD, "dumps variables into console");
    }
    public void onEnable() {
        System.out.println("[BH] TEST: " + mc.world.getRegistryKey().getValue().getPath());
        this.setToggled(false);
    }
}
