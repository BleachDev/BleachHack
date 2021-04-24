package bleach.hack.epearledition.module.mods;


import bleach.hack.epearledition.event.events.EventTick;
import bleach.hack.epearledition.module.Category;
import bleach.hack.epearledition.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.MinecraftClient;

public class AutoWalk extends Module {
    MinecraftClient mc = MinecraftClient.getInstance();

    public AutoWalk() {
        super("AutoWalk", KEY_UNBOUND, Category.MOVEMENT, "Automatically walks/flies forward");
    }

    public void onDisable() {
        mc.options.keyForward.setPressed(false);
        super.onDisable();
    }

    @Subscribe
    public void onTick(EventTick event) {
        mc.options.keyForward.setPressed(true);
    }
}

