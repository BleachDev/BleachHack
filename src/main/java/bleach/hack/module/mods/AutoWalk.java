package bleach.hack.module.mods;


import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.MinecraftClient;

public class AutoWalk extends Module {

    public AutoWalk() {
        super("AutoWalk", KEY_UNBOUND, Category.MOVEMENT, "Automatically walks/flies forward");
    }

    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.options.keyForward.setPressed(false);
        super.onDisable();
    }

    @Subscribe
    public void onTick(EventTick event) {
        boolean pause = ((AutoTunnel) ModuleManager.getModule(AutoTunnel.class)).PauseAutoWalk();
        AutoTunnel at = ((AutoTunnel) ModuleManager.getModule(AutoTunnel.class));
        if (!pause && at.isToggled()) {
            mc.options.keyForward.setPressed(true);
        }
        else if (pause || !at.isToggled()) {
            mc.options.keyForward.setPressed(false);
        }
    }
}

