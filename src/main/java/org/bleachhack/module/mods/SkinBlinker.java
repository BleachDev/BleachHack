package org.bleachhack.module.mods;

import java.util.Random;
import net.minecraft.client.render.entity.PlayerModelPart;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;

public class SkinBlinker extends Module {
    public SkinBlinker() {
        super("SkinBlinker", KEY_UNBOUND, ModuleCategory.MISC, "Makes ur skins body parts blink.");
    }

    private final Random random = new Random();

    @BleachSubscribe
    public void onTick(EventTick event)
    { if(random.nextInt(4) != 0)
        return;

        for(PlayerModelPart part : PlayerModelPart.values())
            mc.options.togglePlayerModelPart(part,
                    !mc.options.isPlayerModelPartEnabled(part));
    }

    @Override
    public void onDisable(boolean inWorld) {
        for(PlayerModelPart part : PlayerModelPart.values())
            mc.options.togglePlayerModelPart(part, true);
        super.onDisable(inWorld);
    }
}
